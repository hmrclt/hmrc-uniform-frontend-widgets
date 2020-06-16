import cats.Monad
import cats.implicits._
import scala.language.higherKinds
import ltbs.uniform._, validation._

package object beardtax {

  type BeardLength = (Int,Int)
  type TellTypes = NilTypes
  type AskTypes = Option[MemberOfPublic] :: BeardStyle :: BeardLength :: NilTypes

  def beardProgram[F[_] : Monad](
    interpreter: Language[F, TellTypes, AskTypes],
    hodCall: (BeardStyle, BeardLength) => F[Int]
  ): F[Int] = {
    import interpreter._
    for {
      memberOfPublic <- ask[Option[MemberOfPublic]]("is-public")
      beardStyle     <- ask[BeardStyle]("beard-style", customContent = Map(
        "beard-style" -> {memberOfPublic match {
          case None                              => ("beard-style-sycophantic", Nil)
          case Some(MemberOfPublic(_, sname, _)) => ("beard-style-menacing", List(sname))
        }}
      ))
      beardLength    <- ask[BeardLength]("beard-length-mm", validation = 
        Rule.condAtPath("_2")(x => x._1 <= x._2, "lower.less.than.higher")
      ) emptyUnless memberOfPublic.isDefined
      cost           <- hodCall(beardStyle, beardLength)
    } yield cost
  }

}
