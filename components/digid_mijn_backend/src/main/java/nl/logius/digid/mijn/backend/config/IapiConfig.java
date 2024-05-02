
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

package nl.logius.digid.mijn.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.logius.digid.mijn.backend.client.app.AppClient;
import nl.logius.digid.mijn.backend.client.digid.AccountClient;
import nl.logius.digid.mijn.backend.client.digid.DigidLogClient;
import nl.logius.digid.mijn.backend.client.digid.NsClient;
import okhttp3.HttpUrl;

@Configuration
public class IapiConfig {

    @Value("${urls.internal.app}")
    private String digidAppBaseUrl;

    @Value("${urls.internal.x}")
    private String digidXBaseUrl;

    @Value("${urls.internal.ns}")
    private String digidNsUrl;

    @Value("${iapi.timeout}")
    private int timeout;

    @Value("${iapi.token}")
    private String iapiToken;


    @Bean
    public AppClient appClient() {
        return new AppClient(HttpUrl.get(digidAppBaseUrl), iapiToken, timeout);
    }

    @Bean
    public AccountClient accountClient() {
        return new AccountClient(HttpUrl.get(digidXBaseUrl), iapiToken, timeout);
    }

    @Bean
    public DigidLogClient digiDClient() {
        return new DigidLogClient(HttpUrl.get(digidXBaseUrl), iapiToken, timeout);
    }

    @Bean
    public NsClient nsClient() {
        return new NsClient(HttpUrl.get(digidNsUrl), iapiToken, timeout);
    }
}