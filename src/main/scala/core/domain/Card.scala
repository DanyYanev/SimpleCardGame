package core.domain

import enumeratum._
import enumeratum.values.IntEnum

case class Card(rank: Rank, suit: Suit){
  override def toString: String = s"$rank of $suit"

  def <(other: Card): Boolean = strength < other.strength

  private lazy val strength: Int = rank.strength * 10 + suit.strength
}

sealed abstract class Suit(val strength: Int) extends EnumEntry
object Suit extends Enum[Suit] {
  val values: IndexedSeq[Suit] = findValues

  case object Spades   extends Suit(4)
  case object Hearts   extends Suit(3)
  case object Clubs    extends Suit(2)
  case object Diamonds extends Suit(1)
}

sealed abstract class Rank(val strength: Int) extends EnumEntry
object Rank extends Enum[Rank] {
  val values: IndexedSeq[Rank] = findValues

  case object Ace   extends Rank(13)
  case object King  extends Rank(12)
  case object Queen extends Rank(11)
  case object Jack  extends Rank(10)
  case object Ten   extends Rank(9)
  case object Nine  extends Rank(8)
  case object Eight extends Rank(7)
  case object Seven extends Rank(6)
  case object Six   extends Rank(5)
  case object Five  extends Rank(4)
  case object Four  extends Rank(3)
  case object Three extends Rank(2)
  case object Two   extends Rank(1)
}
