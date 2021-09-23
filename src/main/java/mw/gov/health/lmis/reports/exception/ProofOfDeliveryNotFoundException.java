package mw.gov.health.lmis.reports.exception;

import mw.gov.health.lmis.utils.Message;

import java.util.UUID;

import static mw.gov.health.lmis.reports.i18n.MessageKeys.PROOF_OF_DELIVERY_NOT_FOUND;

public class ProofOfDeliveryNotFoundException extends NotFoundMessageException {

  public ProofOfDeliveryNotFoundException(UUID id) {
    super(new Message(PROOF_OF_DELIVERY_NOT_FOUND, id.toString()));
  }

}
