package beardtax

import scala.concurrent.Future

// This is the contract between our frontend service and some backend
// constructing an interface and then building to it makes everything
// easier to test
trait Hod {
  def costOfBeard(beardStyle: BeardStyle, length: BeardLength): Future[Int]  
}

// Because we don't want to have to implement and run a HoD to demo
// frontend service, we have a dummy implementation. In real-life this
// would probably consume a Play WS instance and sent HTTP requests.
object DummyHod extends Hod {

  def costOfBeard(
    beardStyle: BeardStyle,
    length: BeardLength
  ) = Future.successful(
      beardStyle match {
      case BeardStyle.SoulPatch => length._2 / 10
      case _                    => length._1 + (length._2 - length._1) / 2
    }
  )
}
