package games

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import core.domain.{Card, Rank, Suit}
import core.dto.{DealCard, ForfeitRound, InvalidRequest, LostRound, Request, Response, WonRound}
import fs2.{Pipe, Stream}

import scala.util.Random

object SingleCardGame {

  sealed trait State
  case class GameNotStarted(balance: Int) extends State
  case class CardDealt(balance: Int, card: Card) extends State

  def apply(): IO[Pipe[IO, Request, Response]] = for {
    initialState <- startGame
  } yield { input: Stream[IO, Request] =>
      input
        .evalMapAccumulate(initialState)(handleCommands)
        .map(_._2)
  }

  def handleCommands(s: State, r: Request): IO[(State, Response)] =
    (s, r) match {
      case (state @ GameNotStarted(balance), req @ Request.StartGame) =>
        if (balance < 10)
          (state, InvalidRequest(req)).pure[IO]
        else
          for {
            card <- randomCard()
          } yield
            (CardDealt(balance, card), DealCard(card))

      case (CardDealt(balance, card), Request.Play) =>
        for {
          generatedCard <- randomCard()
        } yield {
          if (card < generatedCard) {
            val newBalance = balance - 10
            (GameNotStarted(newBalance), LostRound(newBalance))
          } else {
            val newBalance = balance + 10
            (GameNotStarted(newBalance), WonRound(newBalance))
          }
        }

      case (CardDealt(balance, _), Request.Fold) =>
        val newBalance = balance - 3
        (GameNotStarted(newBalance), ForfeitRound(newBalance)).pure[IO]

      case (state, request) =>
        (state, InvalidRequest(request)).pure[IO]
    }

  private val startGame: IO[State] = for {
    balance <- obtainBalance()
  } yield GameNotStarted(balance)

  def obtainBalance(): IO[Int] = {
    1000.pure[IO]
  }

  def randomCard(): IO[Card] = for {
    rank <- IO(Random.between(1, 14))
    suit <- IO(Random.between(1, 5))
  } yield Card(Rank.values(rank), Suit.values(suit))
}
