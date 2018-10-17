package org.ergoplatform.nodeView.wallet.requests

import io.circe._
import org.ergoplatform.api.ApiCodecs
import org.ergoplatform.nodeView.wallet.{ErgoAddress, ErgoAddressEncoder}
import org.ergoplatform.settings.ErgoSettings

trait TransactionRequest {
  val fee: Long
}

class TransactionRequestEncoder(settings: ErgoSettings) extends Encoder[TransactionRequest] with ApiCodecs {

  implicit val addressEncoder: Encoder[ErgoAddress] = new ErgoAddressEncoder(settings).encoder

  def apply(request: TransactionRequest): Json = request match {
    case pr: PaymentRequest => new PaymentRequestEncoder(settings)(pr)
    case ar: AssetIssueRequest => new AssetIssueRequestEncoder(settings)(ar)
  }
}

class TransactionRequestDecoder(settings: ErgoSettings) extends Decoder[TransactionRequest] {

  val paymentRequestDecoder: PaymentRequestDecoder = new PaymentRequestDecoder(settings)
  val assetIssueRequestDecoder: AssetIssueRequestDecoder = new AssetIssueRequestDecoder(settings)

  def apply(cursor: HCursor): Decoder.Result[TransactionRequest] =
    Seq(paymentRequestDecoder, assetIssueRequestDecoder)
      .map(_.apply(cursor))
      .find(_.isRight)
      .getOrElse(Left(DecodingFailure("Can not find suitable decoder", cursor.history)))
}
