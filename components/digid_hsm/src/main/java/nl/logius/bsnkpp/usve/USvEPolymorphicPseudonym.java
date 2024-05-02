
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

package nl.logius.bsnkpp.usve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nl.logius.bsnkpp.crypto.BrainpoolP320r1;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROctetStringParser;
import org.bouncycastle.asn1.DLSequenceParser;
import org.bouncycastle.math.ec.ECPoint;

/*------------------------------------------------------------------------
PolymorphicPseudonym ::= SEQUENCE {
    notationIdentifier OBJECT IDENTIFIER (id-BSNk-polymorphic-pseudonym),
    schemeVersion INTEGER,
    schemeKeySetVersion INTEGER,
    creator IA5String,
    recipient IA5String,
    recipientKeySetVersion INTEGER,
    type INTEGER,
    points SEQUENCE (SIZE (3)) OF ECPoint,
}
------------------------------------------------------------------------*/
public class USvEPolymorphicPseudonym {
    protected ASN1ObjectIdentifier notationIdentifier;
    protected int schemeVersion;
    protected int schemeKeyVersion;
    protected String creator;
    protected String recipient;
    protected int recipientKeySetVersion;
    protected char type;  // 'B'=BSN, 'E'= EIDAS
    protected ECPoint[] points;
    protected byte[] encoded = null;
    

    public ASN1ObjectIdentifier getNotationIdentifier() {
        return notationIdentifier;
    }

    public int getSchemeVersion() {
        return schemeVersion;
    }

    public int getSchemeKeyVersion() {
        return schemeKeyVersion;
    }

    public String getCreator() {
        return creator;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getRecipientKeySetVersion() {
        return recipientKeySetVersion;
    }

    public char getIdentifierType() {
        return type;
    }

    public ECPoint[] getPoints() {
        return this.points;
    }

    public byte[] getEncoded() {
        return encoded;
    }

    protected USvEPolymorphicPseudonym(ASN1ObjectIdentifier notationIdentifier, int schemeVersion, int schemeKeyVersion, String creator, String recipient, int recipientKeySetVersion, ECPoint[] points, char type) {
        this.notationIdentifier = notationIdentifier;
        this.schemeVersion = schemeVersion;
        this.schemeKeyVersion = schemeKeyVersion;
        this.creator = creator;
        this.recipient = recipient;
        this.recipientKeySetVersion = recipientKeySetVersion;
        this.points = points;
        this.type = type;
    }

   
    public static USvEPolymorphicPseudonym decode(byte[] encoded) throws Exception {

        ASN1StreamParser parser = new ASN1StreamParser(encoded);
        DLSequenceParser sequenceParser = (DLSequenceParser) parser.readObject();
        ASN1Encodable readObject = sequenceParser.readObject();
        if (!(readObject instanceof ASN1ObjectIdentifier)) {
            throw new IOException("unexpected structure");
        }
        ASN1ObjectIdentifier _oid = (ASN1ObjectIdentifier) readObject;

        int _schemeVersion = ((ASN1Integer) sequenceParser.readObject()).getValue().intValue(); // schemeVersion
        int _schemeKeyVersion = ((ASN1Integer) sequenceParser.readObject()).getValue().intValue(); // schemeKeyVersion
        String _creator = ((DERIA5String) sequenceParser.readObject()).getString(); // creator
        String _recipient = ((DERIA5String) sequenceParser.readObject()).getString();
        int _recipientKeysetVersion = ((ASN1Integer) sequenceParser.readObject()).getValue().intValue(); // recipientKeySetVersion
        char _type = (char) ((ASN1Integer) sequenceParser.readObject()).getValue().intValue();
        DLSequenceParser ecPointsDerSequenceParser = (DLSequenceParser) parser.readObject();
        ECPoint[] _points = decodePoints(ecPointsDerSequenceParser, 3);

        
        USvEPolymorphicPseudonym pp = new USvEPolymorphicPseudonym(_oid, _schemeVersion, _schemeKeyVersion, _creator, _recipient, _recipientKeysetVersion, _points, _type);
        pp.encoded = encoded;
        return pp;

    }

    private static ECPoint[] decodePoints(DLSequenceParser sequenceParser, int numberOfPoints) throws IOException {
        List<ECPoint> result = new ArrayList<>(numberOfPoints);
        DEROctetStringParser stringParser = (DEROctetStringParser) sequenceParser.readObject();
        while (stringParser != null) {
            DEROctetString octetString = (DEROctetString) stringParser.toASN1Primitive();
            byte[] octets = octetString.getOctets();
            ECPoint ecPoint = BrainpoolP320r1.CURVE.decodePoint(octets);
            result.add(ecPoint);
            stringParser = (DEROctetStringParser) sequenceParser.readObject();
        }
        return result.toArray(new ECPoint[0]);
    }

    public void validate() throws Exception {
        if (!notationIdentifier.getId().equalsIgnoreCase(USvEConstants.OID_BSNK_PP)) {
            throw new Exception("[USvEPolymorphicPseudonym::validate] Invalid OID (expected " + USvEConstants.OID_BSNK_PP + " instead of " + notationIdentifier.getId() + ")");
        }
    }
}
