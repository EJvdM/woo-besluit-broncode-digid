
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

package nl.logius.digid.eid.controller;

import nl.logius.digid.eid.Application;
import nl.logius.digid.eid.models.rest.app.*;
import nl.logius.digid.eid.service.RDWService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "default", "unit-test" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { Application.class })
public class RDWControllerTest {
    @Mock
    private RDWService rdwServiceMock;

    @InjectMocks
    private RDWController rdwController;

    @Test
    public void getCertificateRestServiceTest() {
        GetCertificateResponse expectedResponse = new GetCertificateResponse();
        when(rdwServiceMock.getCertificateRestService(any(GetCertificateRequest.class), anyString())).thenReturn(expectedResponse);

        GetCertificateResponse actualResponse = rdwController.getCertificateRestService(new GetCertificateRequest(), "");

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void prepareEacRequestRestServiceTest() {
        PolyInfoResponse expectedResponse = new PolyInfoResponse();
        when(rdwServiceMock.validatePolymorphInfoRestService(any(PolyInfoRequest.class), anyString())).thenReturn(expectedResponse);

        PolyInfoResponse actualResponse = rdwController.validatePolymorphInfoRestService(new PolyInfoRequest(), "");

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getDigitalSignatureRestServiceTest() {
        SignatureResponse expectedResponse = new SignatureResponse();
        when(rdwServiceMock.getDigitalSignatureRestService(any(SignatureRequest.class), anyString())).thenReturn(expectedResponse);

        SignatureResponse actualResponse = rdwController.getDigitalSignatureRestService(new SignatureRequest(), "");

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void generateSecureAPDUsRestServiceTest() {
        SecApduResponse expectedResponse = new SecApduResponse();
        when(rdwServiceMock.generateSecureAPDUsRestService(any(SecApduRequest.class), anyString())).thenReturn(expectedResponse);

        SecApduResponse actualResponse = rdwController.generateSecureAPDUsRestService(new SecApduRequest(), "");

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getPolymorphicDataRestServiceTest() {
        PolyDataResponse expectedResponse = new PolyDataResponse();
        when(rdwServiceMock.getPolymorphicDataRestService(any(PolyDataRequest.class), anyString())).thenReturn(expectedResponse);

        PolyDataResponse actualResponse = rdwController.getPolymorphicDataRestService(new PolyDataRequest(), "");

        assertEquals(expectedResponse, actualResponse);
    }
}
