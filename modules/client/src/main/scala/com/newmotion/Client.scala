package com.newmotion

import cats.effect._
import cats.effect.concurrent.Ref
import com.newmotion.api.{ChargeStationAuthenticator, _}
import higherkindness.mu.rpc._

object Client extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    new program[IO](cats.effect.Console.io).run.as(ExitCode.Success)
}

private[newmotion] class program[F[_]: ContextShift](
  console: Console[F]
)(implicit F: ConcurrentEffect[F]) {

  import cats.syntax.flatMap._
  import cats.syntax.functor._
  import cats.syntax.applicativeError._
  import cats.syntax.monad._
  import cats.syntax.apply._
  import console._

  private val channelFor: ChannelFor = ChannelForAddress("localhost", 12345)

  private val serviceClient: Resource[F, ChargeStationAuthenticator[F]] =
    ChargeStationAuthenticator.client[F](channelFor)

  private val definition =
    for {
      _        <- putStr("Please enter your serial: ")
      serial   <- readLn
      _        <- putStr("Please enter your password: ")
      password <- readLn
      request = AuthenticateRequest(serial, password)
      _        <- putStrLn(request.toString)
      response <- serviceClient.use(c => c.Authenticate(request))
      serverMood = if (response.authenticated) "authenticated" else "not authenticated"
      _ <- putStrLn(serverMood)
    } yield ()

  private val shieldedDefinition =
    definition.recoverWith {
      case e: RuntimeException =>
        putStrLn(s"error: ${e.getMessage}")
    }

  val run: F[Unit] =
    Ref.of[F, Int](2).flatMap { ref =>
      (shieldedDefinition *> ref.update(cnt => cnt - 1))
        .whileM_(ref.get.map(_ > 0))
    }
}
