
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

package nl.logius.digid.sharedlib.client;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.logius.digid.sharedlib.filter.IapiTokenFilter;
import nl.logius.digid.sharedlib.filter.RequestToken;
import nl.logius.digid.sharedlib.filter.RequestTokenFilter;
import okhttp3.HttpUrl;
import okhttp3.Request;

public abstract class IapiClient extends JsonClient {
    private final String authToken;

    public IapiClient(HttpUrl baseUrl, String authToken, int timeout, ObjectMapper objectMapper) {
        super(baseUrl, timeout, objectMapper);
        this.authToken = authToken;
    }

    public IapiClient(HttpUrl baseUrl, String authToken, int timeout) {
        super(baseUrl, timeout);
        this.authToken = authToken;
    }

    @Override
    protected HttpUrl toUrl(String path, Map<String, String> queryParams) {
        return super.toUrl("iapi/" + path, queryParams);
    }

    protected HttpUrl toUrl(String path) {
        return this.toUrl(path, null);
    }

    @Override
    protected void headers(HttpUrl url, Request.Builder builder) {
        builder.addHeader(IapiTokenFilter.TOKEN_HEADER, authToken);
        builder.addHeader(RequestTokenFilter.REQUEST_TOKEN_HEADER, RequestToken.get().toString());
    }
}
