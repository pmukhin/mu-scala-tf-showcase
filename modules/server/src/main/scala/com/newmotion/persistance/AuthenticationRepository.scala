package com.newmotion.persistance

import cats.effect.Sync
import com.newmotion.models._

trait AuthenticationRepository[F[_]] {
  def findByIdAndPassword(
    chargeStationId: ChargeStationId,
    password: Password
  ): F[Boolean]
}

object AuthenticationRepository {
  object fromMap {
    type Key = (String, String)

    private class Repo[F[_]](
      mapPwd: Map[Key, Boolean]
    )(implicit F: Sync[F])
        extends AuthenticationRepository[F] {
      override def findByIdAndPassword(
        chargeStationId: ChargeStationId,
        password: Password
      ): F[Boolean] =
        F.pure(
          mapPwd.getOrElse((chargeStationId.value, password.value), false)
        )
    }

    def apply[F[_]](m: Map[Key, Boolean])(implicit F: Sync[F]): F[AuthenticationRepository[F]] =
      F.pure(new Repo(m))
  }
}
