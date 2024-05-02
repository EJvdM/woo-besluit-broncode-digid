
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

package nl.logius.digid.app.client;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.logius.digid.app.shared.Util;
import nl.logius.digid.sharedlib.client.IapiClient;
import nl.logius.digid.sharedlib.exception.ClientException;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static nl.logius.digid.app.shared.Constants.*;

public class EidClient extends IapiClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String sourceIpSalt;

    public EidClient(HttpUrl url, String authToken, int timeout, String sourceIpSalt) {
        super(url, authToken, timeout);
        this.sourceIpSalt = sourceIpSalt;
    }

    public Map<String, String> startSession(String returnUrl, String confirmId, String clientIpAddress){

        final var body = Map.of(
            RETURN_URL, returnUrl,
            CONFIRM_ID, confirmId,
            CLIENT_IP_ADDRESS, Util.anonimizedIp(clientIpAddress, sourceIpSalt)
        );

        try {
            return mapper.convertValue(execute("new", body), new TypeReference<>() {});
        } catch (ClientException e) {
            logger.error("Exception while parsing response from digid_eid: {}", e.getMessage());
        }

        return Map.of();
    }
}
