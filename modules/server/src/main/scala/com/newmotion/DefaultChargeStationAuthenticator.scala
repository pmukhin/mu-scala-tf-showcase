package com.newmotion

import cats.effect.Sync
import com.newmotion.action.Authenticate
import com.newmotion.api.ChargeStationAuthenticator
import com.newmotion.persistance.AuthenticationRepository

class DefaultChargeStationAuthenticator[F[_]: Sync](
  authRepo: AuthenticationRepository[F]
) extends Authenticate[F](authRepo)
    with ChargeStationAuthenticator[F]
