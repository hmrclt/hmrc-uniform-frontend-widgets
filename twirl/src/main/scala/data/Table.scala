package uk.gov.hmrc.uniformwidgets.data

case class Cell[A](
  value: A,
  heading: Boolean = false,
  alignRight: Boolean = false,
  customWidth: Option[Either[Int, String]] = None
) {
  def map[B](f: A => B): Cell[B] = Cell(
    f(value),
    heading,
    alignRight,
    customWidth
  )
}

case class Table[A](
  data: Iterable[Iterable[Cell[A]]]
) {

  def map[B](f: A => B): Table[B] = Table(
    data.map{_.map{_.map(f)}}
  )

  def mapCells[B](f: Function[(Int, Int, Cell[A]),Cell[B]]): Table[B] = Table(
    data.map(_.zipWithIndex).zipWithIndex map {
      case (cells, y) => cells.map {
        case (cell, x) => f((x,y,cell))
      }
    }
  )

  def modify(f: PartialFunction[(Int, Int, Cell[A]),Cell[A]]): Table[A] =
    mapCells{
      case (x,y,c) if f.isDefinedAt((x,y,c)) => f((x,y,c))
      case (_,_,c) => c
    }


}
