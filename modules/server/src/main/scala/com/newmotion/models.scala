package com.newmotion

import cats.data.{NonEmptyList, Validated, ValidatedNel}

object models {
  case class ChargeStationId private (value: String) extends AnyVal

  case object validationErrors {
    sealed trait Invalid { def field: String }

    case class TooShort(field: String) extends Invalid
  }

  object ChargeStationId {
    import validationErrors._

    def fromString(v: String): ValidatedNel[Invalid, ChargeStationId] =
      Validated.cond(v.length > 2, ChargeStationId(v), TooShort("chargeStationId")).toValidatedNel
  }

  case class Password private (value: String) extends AnyVal
  object Password {
    import validationErrors._

    def fromString(v: String): ValidatedNel[Invalid, Password] =
      Validated.cond(v.length > 2, Password(v), TooShort("password")).toValidatedNel
  }

  object syntax {
    private val sep = "\n"

    implicit class NelInvalidOps(val nel: NonEmptyList[validationErrors.Invalid]) extends AnyVal {
      private def getText: PartialFunction[validationErrors.Invalid, String] = {
        case validationErrors.TooShort(_) => "value too short"
      }

      def prettyPrint: String =
        s"validation errors occurred:$sep" ++ nel
          .map(v => s"invalid data supplied for field ${v.field}: %s".format(getText(v)))
          .toList
          .mkString(sep)
    }
  }

}
