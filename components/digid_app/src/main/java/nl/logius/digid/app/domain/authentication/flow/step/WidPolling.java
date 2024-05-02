
/*
  Deze broncode is openbaar gemaakt vanwege een Woo-verzoek zodat deze
  gericht is op transparantie en niet op hergebruik. Hergebruik van 
  de broncode is toegestaan onder de EUPL licentie, met uitzondering 
  van broncode waarvoor een andere licentie is aangegeven.
  
  Het archief waar dit bestand deel van uitmaakt is te vinden op:
    https://github.com/MinBZK/woo-besluit-broncode-digid
  
  Eventuele kwetsbaarheden kunnen worden gemeld bij het NCSC via:
    https://www.ncsc.nl/contact/kwetsbaarheid-melden
  onder vermelding van "Logius, openbaar gemaakte broncode DigiD" 
  
  Voor overige vragen over dit Woo-besluit kunt u mailen met open@logius.nl
  
  This code has been disclosed in response to a request under the Dutch
  Open Government Act ("Wet open Overheid"). This implies that publication 
  is primarily driven by the need for transparence, not re-use.
  Re-use is permitted under the EUPL-license, with the exception 
  of source files that contain a different license.
  
  The archive that this file originates from can be found at:
    https://github.com/MinBZK/woo-besluit-broncode-digid
  
  Security vulnerabilities may be responsibly disclosed via the Dutch NCSC:
    https://www.ncsc.nl/contact/kwetsbaarheid-melden
  using the reference "Logius, publicly disclosed source code DigiD" 
  
  Other questions regarding this Open Goverment Act decision may be
  directed via email to open@logius.nl
*/

package nl.logius.digid.app.domain.authentication.flow.step;

import nl.logius.digid.app.domain.authentication.response.WidPollResponse;
import nl.logius.digid.app.domain.flow.*;
import nl.logius.digid.app.shared.request.AppRequest;
import nl.logius.digid.app.shared.response.AppResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class WidPolling extends AbstractFlowStep<AppRequest> {

    private final boolean attestEnabled;

    public WidPolling(boolean attestEnabled) {
        this.attestEnabled = attestEnabled;
    }

    private final String ACTIVE = "active";
    private final String ISSUED = "issued";

    private final String[] allowedActions = new String[] { "activate_driving_licence", "activate_identity_card" };

    @Override
    public AppResponse process(Flow flow, AppRequest request) throws FlowNotDefinedException, IOException, NoSuchAlgorithmException {
        var response = new WidPollResponse(attestEnabled && Arrays.asList(allowedActions).contains(appSession.getAction()));

        setValid(false);

        switch (appSession.getState()) {
            case "VERIFIED" -> {
                if (validateCardStatus()) {
                    setValid(true);
                    response.setStatus(appSession.getState());
                }
            }
            case "COMPLETED", "AUTHENTICATED", "CONFIRMED", "CANCELLED", "ABORTED" -> response.setStatus(appSession.getState());
            default -> response.setStatus("PENDING");
        }

        if ("ABORTED".equals(appSession.getState())) {
            response.setStatus("ABORTED");
            response.setError(appSession.getError());
        }

        return response;
    }

    private boolean validateCardStatus() {
        if (ACTIVE.equals(appSession.getCardStatus()) || (ISSUED.equals(appSession.getCardStatus()) && Arrays.asList(allowedActions).contains(appSession.getAction()))) {
            return true;
        }

        else if (ISSUED.equals(appSession.getCardStatus())) {
            appSession.setState("ABORTED");
            appSession.setError("msc_issued");
        }

        else {
            appSession.setState("ABORTED");
            appSession.setError("msc_inactive");
        }

        return false;
    }

    @Override
    public boolean expectAppAuthenticator() {
        return false;
    }
}