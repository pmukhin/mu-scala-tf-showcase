package com.newmotion

import cats.effect._
import com.newmotion.persistance.AuthenticationRepository
import higherkindness.mu.rpc.server._

object Server extends IOApp {

  private val service =
    AuthenticationRepository
      .fromMap[IO](Map(("F012", "test1") -> true))
      .map(new DefaultChargeStationAuthenticator[IO](_))

  def run(args: List[String]): IO[ExitCode] =
    service.flatMap { implicit service =>
      import com.newmotion.api._
      for {
        serviceDef <- ChargeStationAuthenticator.bindService[IO]
        server     <- GrpcServer.default[IO](12345, List(AddService(serviceDef)))
        _          <- GrpcServer.server[IO](server)
      } yield ExitCode.Success
    }
}
