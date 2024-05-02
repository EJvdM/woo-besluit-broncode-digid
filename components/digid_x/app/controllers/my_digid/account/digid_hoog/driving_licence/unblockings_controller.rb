
# Deze broncode is openbaar gemaakt vanwege een Woo-verzoek zodat deze
# gericht is op transparantie en niet op hergebruik. Hergebruik van 
# de broncode is toegestaan onder de EUPL licentie, met uitzondering 
# van broncode waarvoor een andere licentie is aangegeven.
# 
# Het archief waar dit bestand deel van uitmaakt is te vinden op:
#   https://github.com/MinBZK/woo-besluit-broncode-digid
# 
# Eventuele kwetsbaarheden kunnen worden gemeld bij het NCSC via:
#   https://www.ncsc.nl/contact/kwetsbaarheid-melden
# onder vermelding van "Logius, openbaar gemaakte broncode DigiD" 
# 
# Voor overige vragen over dit Woo-besluit kunt u mailen met open@logius.nl
# 
# This code has been disclosed in response to a request under the Dutch
# Open Government Act ("Wet open Overheid"). This implies that publication 
# is primarily driven by the need for transparence, not re-use.
# Re-use is permitted under the EUPL-license, with the exception 
# of source files that contain a different license.
# 
# The archive that this file originates from can be found at:
#   https://github.com/MinBZK/woo-besluit-broncode-digid
# 
# Security vulnerabilities may be responsibly disclosed via the Dutch NCSC:
#   https://www.ncsc.nl/contact/kwetsbaarheid-melden
# using the reference "Logius, publicly disclosed source code DigiD" 
# 
# Other questions regarding this Open Goverment Act decision may be
# directed via email to open@logius.nl

# frozen_string_literal: true

module MyDigid
  module Account
    module DigidHoog
      module DrivingLicence
        class UnblockingsController < BaseController
          include FlowBased
          include AppLinkHelper
          include AppAuthenticationSession
          include ApplicationHelper
          include RdwClient

          before_action :check_switches, except: [:switch_off_message]
          before_action :set_flow, :check_code_blocked, only: [:use_unblockings_code]
          before_action :render_not_found_if_account_deceased

          ## AANVRAGEN BRIEF DEBLOKKERINGSCODE
          # D48 - Mijn DigiD | Deblokkeringscode aanvragen
          def index
            current_flow.transition_to!(:confirm)
            @page_header = t("request_unblockcode_wid", wid_type: t("document.driving_licence"))
            @page_name = "D48"
            @page_title = t("titles.D48")
          end

          def cancel
            Log.instrument("1030", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
            redirect_to my_digid_url
          end

          def switch_off_message
            Log.instrument("1414", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
            flash.now[:alert] = t("digid_hoog.request_unblock_letter.abort.wid_switch_off", wid_type: t("document.driving_licence")).html_safe
            render_simple_message(ok: my_digid_url)
          end

          def finalize
            current_flow.transition_to!(:completed)
            reset_flow
            session_fixation

            registration = Registration.find(session[:registration_id])
            registration.update_attribute(:status, ::Registration::Status::REQUESTED)
            registration.update_letters_to_finished_and_expiration(geldigheidstermijn_deblokkeringscode_brief, ActivationLetter::LetterType::AANVRAAG_DEBLOKKERINGSCODE_RIJBEWIJS)

            unblock_code = registration.activation_letters.last.controle_code
            registration.wid.update_attribute(:unblock_code, unblock_code)

            Log.instrument("984", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
            Log.instrument("985", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)

            if current_account.email_activated?
              NotificatieMailer.delay(queue: "email").notify_aanvraag_deblokkeringscode_rijbewijs(account_id: current_account.id, recipient: current_account.adres)
            elsif current_account.phone_number.present?
              current_account.with_language { sms_service.send_sms(message: t("sms_message.SMS16", wid_type: t("document.driving_licence")), spoken: false) }
            end
            redirect_to extension_confirmation_url
          end

          ## INVOEREN DEBLOKKERINGSCODE BRIEF
          # D50 – Mijn DigiD | Deblokkeren inlogfunctie rijbewijs - deblokkeringscode
          def use_unblockings_code
            return redirect_to my_digid_url if !driving_licence_enabled?
            Log.instrument("991", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)

            @unblockingscode = Unblockingscode.new
            @unblockingscode.set_error("blank") if flash[:unblock_blank]
            @unblockingscode.set_error("wrong") if flash[:unblock_wrong]

            @page_header = t("unblock_driving_licence")
            @page_name = "D50"
            @page_title = t("titles.D50.driving_licence")
          end

          def verify_code
            @unblockingscode = Unblockingscode.new
            @wid = Wid.where(sequence_no: current_flow[:sequence_no_chosen], card_type: current_flow[:card_type_chosen], action: "unblock").where.not(unblock_code: nil).last
            current_flow[:verify_code][:wid] = @wid

            filled_in_code = params[:unblockingscode][:unblockingscode].upcase

            if filled_in_code.blank?
              Log.instrument("995", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
              flash[:unblock_blank] = true
              return redirect_to my_digid_licence_unblocking_code_invoeren_url(card_type: current_flow[:card_type_chosen], sequence_no: current_flow[:sequence_no_chosen])
            elsif @wid.unblock_code == filled_in_code && !@wid.blocking_manager_for_unblocking.blocked? && !unblocking_letter_expired?(@wid)
              current_flow.transition_to!(:verify_code)
              current_flow.complete_step!(:verify_code, controller: self)
              current_flow.transition_to!(:choose_card_reader)
              return redirect_to my_digid_licence_unblocking_choose_cardreader_url
            else
              @wid.blocking_manager_for_unblocking.register_failed_attempt!

              if unblocking_letter_expired?(@wid)
                current_flow[:error] = "code_expired"
                return redirect_to current_flow[:redirect_to][:abort_url]
              elsif @wid.blocking_manager_for_unblocking.blocked?
                current_flow[:error] = "update_blocked"
                return redirect_to current_flow[:redirect_to][:abort_url]
              end

              Log.instrument("993", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
              flash[:unblock_wrong] = t("digid.933")
              return redirect_to my_digid_licence_unblocking_code_invoeren_url(card_type: current_flow[:card_type_chosen], sequence_no: current_flow[:sequence_no_chosen])
            end
          end

          # D56 - Uitlezen eID met USB
          def read_out_usb
            current_flow.transition_to!(:scan_wid)
            session.delete(:resolve_before) if session[:resolve_before]
            Log.instrument("997", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
            @page_name = "D56"
            @page_title = t("titles.D56.unblock.driving_licence")
            @page_header = t("unblock_driving_licence")
            @app_session_id = session[:app_session_id]
          end

          # D29 - Mijn DigiD | Deblokkeren inlogfunctie rijbewijs - apparaat selecteren
          def choose_cardreader
            current_flow.transition_to!(:choose_card_reader)

            @page_header = t("unblock_driving_licence")
            @page_name = "D29"
            @page_title = t("titles.D29.unblock.driving_licence")

            if mobile_browser?
              current_flow[:redirect_to][:web_to_app_url] = my_digid_licence_unblocking_start_new_session_url
              current_flow[:redirect_to][:new_session_url] = my_digid_licence_unblocking_new_session_url(card_reader_type: "app")
              redirect_to my_digid_choose_device_wid_url
            end
          end

          def new
            if params[:card_reader_type] == "app"
              current_flow.transition_to!(:app_chosen)
              session[:authentication][:web_to_app] = mobile_browser?
              session[:chosen_method] = "app"
              Log.instrument("996", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
              @page_name = "D57"
              @page_title = t("titles.D57.unblock.driving_licence")
              @page_header = t("unblock_driving_licence")
              @code = AppVerificationCode.new
              render :new
            else
              session[:chosen_method] = "usb"
              current_flow.transition_to!(:usb_chosen)
              redirect_to my_digid_licence_unblocking_start_new_session_url
            end
          end

          def create
            begin
              options = { action: "reactivate_driving_license", return_url: my_digid_wid_app_done_url }
              options[:app_ip] = request.remote_ip if session[:chosen_method] == "usb" || session[:chosen_method] == "web_to_app"
              new_wid_app_session(**options)
            rescue DigidUtils::Iapi::Error
              return redirect_to current_flow[:redirect_to][:abort_url]
            end

            if session[:chosen_method] == "web_to_app"
              Log.instrument("980", account_id: current_account.id, hidden: true)
              human_process = t("process_names.log.unblock_wid", locale: :nl, wid_type: t("document.driving_licence", locale: :nl))
              unless browser_supported_for_web_to_app?
                Log.instrument("1548", human_process: human_process, account_id: current_account&.id)
                flash.now[:notice] = t("ios_browser_not_supported_for_my_digid_with_app").html_safe
                return render_simple_message(previous: my_digid_url)
              end
              session[:wid_web_to_app_cancel_to] = current_flow[:redirect_to][:cancel_url]
              redirect_to digid_app_link(digid_app_provisioning_uri("wid", session[:app_session_id]))
            elsif session[:chosen_method] == "usb"
              redirect_to my_digid_licence_unblocking_read_out_usb_url
            else
              redirect_to my_digid_licence_unblocking_scan_qr_url
            end
          end

          # D42 - Mijn DigiD | Deblokkeren inlogfunctie rijbewijs - scannen QR-code
          def scan_qr
            current_flow.transition_to!(:app_chosen)
            current_flow.transition_to!(:scan_qr_code)
            session[:chosen_method] = "app"
            @app_session_id = session[:app_session_id]
            @page_header = t("unblock_driving_licence")
            @page_name = "D42"
            @page_title = t("titles.D42.unblock.driving_licence")
          end

          def cancel_unblocking
            current_flow.transition_to!(:failed)
            Log.instrument("1031", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
            cancel_eid_session
            app_session&.cancel!
            reset_flow
            redirect_to my_digid_url
          end

          # D45 - Mijn DigiD | Deblokkeren inlogfunctie rijbewijs - uitlezen eID
          def scan_wid
            session.delete(:resolve_before) if session[:resolve_before]
            current_flow.transition_to!(:scan_wid)
            @page_header = t("unblock_driving_licence")
            @page_name = "D45"
            @page_title = t("titles.D45.unblock.driving_licence")
          end

          # D51 - Mijn DigiD | Deblokkeren inlogfunctie rijbewijs - gelukt
          def success
            current_flow.transition_to!(:completed)
            @page_header = t("unblock_driving_licence")
            @page_name = "D51"
            @page_title = t("titles.D51.driving_licence")
            Log.instrument("1007", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
            app_session&.complete!
            complete_flow
            flash[:notice] = t("digid_hoog.unblock.success", wid_type: t("document.driving_licence"))
          end

          def abort
            current_flow.transition_to!(:failed)
            app_session&.abort!
            simple_message_options = { ok: my_digid_url }

            default_key = wid_notice_key(:unblock, error: app_session&.error || current_flow[:error])
            human_process = t("process_names.notice.unblock_wid", wid_type: t("document.driving_licence"))

            message = case app_session&.error || current_flow[:error]
                      when "false_bsn_licence", "false_bsn_id_card"
                        Log.instrument("986", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
                        t("digid_hoog.unblock.abort.false_bsn_licence")
                      when "update_blocked"
                        Log.instrument(default_key, wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
                        t(default_key, max_amount: ::Configuration.get_int("pogingen_deblokkeringscode_identiteitsbewijs"), account_id: current_account.id)
                      when "code_expired"
                        Log.instrument("994", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
                        t(default_key, expiration_period: geldigheidstermijn_deblokkeringscode_brief, account_id: current_account.id)
                      when "mu_error"
                        Log.instrument("1181", wid_type: t("document.driving_licence", locale: :nl), mu: "RDW", account_id: current_account.id, hidden: true)
                        t(default_key, wid_type: t("document.driving_licence")).html_safe
                      when "msc_not_issued"
                        Log.instrument("987", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
                        t(default_key, wid_type: t("document.driving_licence")).html_safe
                      when "eid_timeout"
                        Log.instrument("1060", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "no_app_session"
                        Log.instrument(default_key, wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "wrong_doctype_sequence_no"
                        Log.instrument(default_key, wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
                        text_id = current_flow[:card_type_chosen] == app_session.document_type ? "digid_hoog.unblock.right_doctype_wrong_sequence_no_licence" : "digid_hoog.unblock.failed_wrong_doctype"
                        t(text_id,
                          doctype_chosen: t("document.driving_licence"),
                          doctype_scanned: app_session.document_type == "NL-Rijbewijs" ? t("document.driving_licence") : t("document.id_card"),
                          sequence_no_chosen: mask_personal_number(current_flow[:sequence_no_chosen])
                        )
                      when "desktop_client_timeout"
                        Log.instrument("1061", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
                        t(default_key, url: desktop_client_download_link)
                      when "desktop_clients_ip_address_not_equal"
                        Log.instrument("1064", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "cancelled_in_app"
                        Log.instrument("1031", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "no_nfc"
                        Log.instrument("1197", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "nfc_forbidden"
                        Log.instrument("1198", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "verification_code_invalid"
                        Log.instrument("1420", human_process: t("process_names.log.unblock_wid", wid_type: t("document.driving_licence", locale: :nl), locale: :nl), hidden: true)
                        t("digid_app.verification_code_invalid", human_process: human_process)
                      when "pin_blocked"
                        Log.instrument("1084", wid_type: t("document.driving_licence", locale: :nl), hidden: true)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "puk_blocked"
                        Log.instrument("1086", wid_type: t("document.driving_licence", locale: :nl), hidden: true)
                        t(default_key, wid_type: t("document.driving_licence"))
                      when "activation_needed"
                        Log.instrument("1087", wid_type: t("document.driving_licence", locale: :nl), hidden: true)
                        t(default_key, wid_type: t("document.driving_licence"))
                      else
                        Log.instrument("1070", wid_type: t("document.driving_licence", locale: :nl))
                        t("digid_hoog.activate.abort.unknown_error", wid_type: t("document.driving_licence"))
                      end

            flash.now[:notice] = message.html_safe
            render_simple_message(simple_message_options)
            reset_flow
          end

          def web_to_app?
            session[:authentication] && session[:authentication][:web_to_app]
          end

          def update_status!
            current_flow.transition_to!(:update_status)
            begin
              rdw_client.update(bsn: current_account.bsn, status: "Actief", sequence_no: current_flow[:wid_scanned][:sequence_no])
            rescue
              current_flow.transition_to!(:failed)
              current_flow[:error] = "mu_error"
              return redirect_to current_flow[:redirect_to][:abort_url]
            end

            if current_account.email_activated?
              NotificatieMailer.delay(queue: "email").notify_rijbewijs_gedeblokkeerd(account_id: current_account.id, recipient: current_account.adres)
            elsif current_account.phone_number.present?
              current_account.with_language do
                sms_service.send_sms(message: t("sms_message.SMS14", wid_type: t("document.driving_licence")), spoken: false)
              end
            end

            current_flow[:verify_code][:wid].update_attribute(:unblock_code, nil)
            redirect_to my_digid_licence_unblocking_success_url
          end

          private

          def check_code_blocked
            @wid = Wid.where(sequence_no: current_flow[:sequence_no_chosen], card_type: current_flow[:card_type_chosen], action: "unblock")&.where&.not(unblock_code: nil)&.last
            return unless @wid.blocking_manager_for_unblocking.blocked? || @wid.nil?

            current_flow[:error] = "update_blocked"
            return redirect_to current_flow[:redirect_to][:abort_url]
          end


          def set_flow
            session[:flow] = UnblockDrivingLicenceFlow.new
            session[:current_flow] = "unblock_licence"

            current_flow[:sequence_no_chosen] = params[:sequence_no]
            current_flow[:card_type_chosen] = params[:card_type]

            current_flow[:redirect_to][:abort_url] = my_digid_licence_unblocking_abort_url
            current_flow[:redirect_to][:continue_after_qr_url] = my_digid_licence_unblocking_scan_wid_url
            current_flow[:redirect_to][:cancel_url] = my_digid_licence_unblocking_cancel_url
            current_flow[:redirect_to][:update_status_url] = my_digid_licence_unblocking_update_status_url
          end

          def sms_service
            @sms_service ||= SmsChallengeService.new(account: current_account)
          end

          def check_switches
            if request.referer
              return if driving_licence_enabled? && show_driving_licence?(current_account.bsn)
              return redirect_via_js_or_http(my_digid_licence_unblocking_switch_off_message_path) if session[:current_flow] == "request_unblock_letter_licence"

              Log.instrument("998", wid_type: t("document.driving_licence", locale: :nl), account_id: current_account.id, hidden: true)
              flash.now[:alert] = t("digid_hoog.unblock.abort.wid_switch_off", wid_type: t("document.driving_licence")).html_safe
              render_simple_message(ok: my_digid_url)
            else
              check_tonen_rijbewijs_switch
            end
          end

          def geldigheidstermijn_deblokkeringscode_brief
            ::Configuration.get_int("geldigheidstermijn_deblokkeringscode_brief")
          end

          def unblocking_letter_expired?(wid)
            Time.zone.now > (wid.created_at + geldigheidstermijn_deblokkeringscode_brief.days)
          end
        end
      end
    end
  end
end
