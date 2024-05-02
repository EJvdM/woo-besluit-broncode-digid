
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

package nl.logius.digid.card.asn1.models;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.logius.digid.card.ObjectUtils;
import nl.logius.digid.card.asn1.converters.BaseConverterTest;

public class EcSignatureTest extends BaseConverterTest {
    @Test
    public void byteConstructor() {
        assertEquals(new EcSignature(4, BigInteger.ONE, BigInteger.TEN),
            new EcSignature(new byte[] { 0, 0, 0, 1, 0, 0, 0, 10}));
    }

    @Test
    public void shouldEncode() {
        assertArrayEquals(new byte[] { 0, 0, 0, 1, 0, 0, 0, 10},
            new EcSignature(4, BigInteger.ONE, BigInteger.TEN).getEncoded());
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(EcSignature.class)
            .verify();
    }

    @Test
    public void serialization() {
        final EcSignature signature = new EcSignature(4, BigInteger.ONE, BigInteger.TEN);
        assertEquals(signature, ObjectUtils.deserialize(ObjectUtils.serialize(signature)));
    }
}
