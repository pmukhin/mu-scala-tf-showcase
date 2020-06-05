package com.newmotion.action

import cats.data._
import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.functor._
import com.newmotion.api._
import com.newmotion.models._
import com.newmotion.persistance.AuthenticationRepository

class Authenticate[F[_]: Sync](authRepo: AuthenticationRepository[F]) { self: ChargeStationAuthenticator[F] =>

  import com.newmotion.models.syntax._

  case class BadRequestError(msg: String) extends RuntimeException(msg)

  private def onInvalid(nel: NonEmptyList[validationErrors.Invalid])(implicit F: Sync[F]) =
    F.raiseError[AuthenticateResponse](
      BadRequestError(nel.prettyPrint)
    )

  private def onValid(fa: F[Boolean]) =
    fa.map(AuthenticateResponse)

  def Authenticate(req: AuthenticateRequest): F[AuthenticateResponse] =
    (ChargeStationId.fromString(req.charge_station_id), Password.fromString(req.password))
      .mapN {
        case (id, pwd) =>
          authRepo.findByIdAndPassword(id, pwd)
      }
      .fold(onInvalid, onValid)

}
