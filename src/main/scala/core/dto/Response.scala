package core.dto

import core.domain.Card
import enumeratum._

sealed trait Request extends EnumEntry
object Request extends Enum[Request] {
  val values: IndexedSeq[Request] = findValues

  case object StartGame extends Request
  case object Play extends Request
  case object Fold extends Request
  case object Quit extends Request
}

sealed trait Response
case class Start(balance: Int) extends Response
case class WonRound(balance: Int) extends Response
case class LostRound(balance: Int) extends Response
case class ForfeitRound(balance: Int) extends Response
case class DealCard(card: Card) extends Response
case class InvalidRequest(request: Request) extends Response
