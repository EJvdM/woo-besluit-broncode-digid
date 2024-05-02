
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

package nl.logius.digid.saml.domain.artifact;

import nl.logius.digid.saml.domain.authentication.RequestAbstractTypeValidator;
import org.jetbrains.annotations.NotNull;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import static org.apache.commons.lang3.StringUtils.ordinalIndexOf;

public record ArtifactResolveRequestValidator(String destination) implements Validator {

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return ArtifactResolve.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors e) {
        var artifactResolve = (ArtifactResolve) target;

        var result = new BeanPropertyBindingResult(artifactResolve, "ArtifactResolve");
        ValidationUtils.invokeValidator(new RequestAbstractTypeValidator(), target, result);

        if (result.hasErrors()) e.addAllErrors(result);

        if (artifactResolve.getArtifact() == null ||
                artifactResolve.getArtifact().getValue() == null ||
                artifactResolve.getArtifact().getValue().isEmpty()) {
            e.rejectValue("artifact", "Must be set");
        }

        if (artifactResolve.getDestination() != null && !artifactResolve.getDestination().startsWith(
                destination.substring(0, ordinalIndexOf(destination, "/", 3)))) {
            e.rejectValue("destination", "URL of the recipient on which the message is offered. MUST match ArtifactResolutionService in the metadata.");
        }

        if (artifactResolve.getSignature() == null) {
            e.rejectValue("signature", "Must be included");
        }
    }
}
