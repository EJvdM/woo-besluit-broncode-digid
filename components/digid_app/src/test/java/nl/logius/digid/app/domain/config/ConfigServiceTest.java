
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

package nl.logius.digid.app.domain.config;

import nl.logius.digid.app.client.SharedServiceClient;
import nl.logius.digid.app.client.SharedServiceClientException;
import nl.logius.digid.app.domain.config.response.ConfigResponse;
import nl.logius.digid.app.domain.config.response.WebServerResponse;
import nl.logius.digid.app.domain.switches.SwitchService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.when;

@ActiveProfiles({"development"})
@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private SharedServiceClient sharedServiceClient;

    @Mock
    private SwitchService switchService;

    @InjectMocks
    private ConfigService configService;

    @Test
    void getWebServerUrlsTest() {

        ConfigService configService = new ConfigService("http", "SSSSSSSSSSSSSSS", sharedServiceClient, switchService);

        WebServerResponse webServerResponse = configService.getWebserverUrls();

        Assertions.assertEquals("OK", webServerResponse.getStatus());
        Assertions.assertEquals("Mijn DigiD", webServerResponse.getServices().get(0).getName());
        Assertions.assertEquals("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS", webServerResponse.getServices().get(0).getUrl());
    }

    @Test
    void getConfigTest() throws SharedServiceClientException {

        when(sharedServiceClient.getSSConfigInt("change_app_pin_maximum_per_day")).thenReturn(2);
        when(sharedServiceClient.getSSConfigInt("snelheid_aanvragen_digid_app")).thenReturn(1);
        when(switchService.digidRdaSwitchEnabled()).thenReturn(true);
        when(switchService.digidRequestStationEnabled()).thenReturn(false);
        when(switchService.digidEhaEnabled()).thenReturn(true);

        ConfigResponse configResponse = configService.getConfig();

        Assertions.assertEquals(1, configResponse.getLetterRequestDelay());
        Assertions.assertEquals(2, configResponse.getMaxPinChangePerDay());
        Assertions.assertTrue(configResponse.isEhaEnabled());
    }
}
