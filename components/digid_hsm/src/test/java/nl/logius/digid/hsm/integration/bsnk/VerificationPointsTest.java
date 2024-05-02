
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

package nl.logius.digid.hsm.integration.bsnk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import nl.logius.digid.hsm.integration.BaseTest;
import nl.logius.digid.sharedlib.filter.IapiTokenFilter;

public class VerificationPointsTest extends BaseTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldReturnPoints() throws Exception {
        final RequestEntity<Map<String, Object>> re = RequestEntity
                .post(new URI("/iapi/bsnk/verification-points"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IapiTokenFilter.TOKEN_HEADER, getToken())
                .body(ImmutableMap.of());
        final ResponseEntity<String> response = restTemplate.exchange(re, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        final ObjectNode result = (ObjectNode) mapper.readTree(response.getBody());
        assertEquals(ImmutableSet.of("IPp", "PPp"), ImmutableSet.copyOf(result.fieldNames()));
        assertEquals("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS", result.get("IPp").asText());
        assertEquals("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS", result.get("PPp").asText());
    }
}
