import Types.{Bit, Digit, Even, NoParity, Odd, One, Parity, Pixel, Str, Zero}

import scala.collection.immutable

object Decoder {
  // TODO 1.1
  def toBit(s: Char): Bit = if (s == '1') One else Zero
  def toBit(s: Int): Bit = if (s == 1) One else Zero

  // TODO 1.2
  def complement(c: Bit): Bit =
    c match {
      case One => Zero;
      case Zero => One;
    }

  // TODO 1.3
  val LStrings: List[String] = List("0001101", "0011001", "0010011", "0111101", "0100011",
    "0110001", "0101111", "0111011", "0110111", "0001011")
  val leftOddList: List[List[Bit]] = LStrings.map(_.map(toBit).toList) // codificări L
  val rightList: List[List[Bit]] = leftOddList.map(_.map(complement)) // codificări R
  val leftEvenList: List[List[Bit]] = rightList.map(_.foldLeft
    (List[Bit]()) ((acc, b) => b::acc)) // codificări  G
  
  // TODO 1.4
  def group[A](l: List[A]): List[List[A]] = l.foldRight(List[List[A]]())((c, acc) =>
      if (acc.isEmpty || c != acc.head.last) List(c)::acc
      else (c :: acc.head) :: acc.tail
  )
  
  // TODO 1.5
  def runLength[A](l: List[A]): List[(Int, A)] = group[A](l).map(sublist => (sublist.size, sublist.last))
  
  case class RatioInt(n: Int, d: Int) extends Ordered[RatioInt] {
    require(d != 0, "Denominator cannot be zero")
    private val gcd = BigInt(n).gcd(BigInt(d)).toInt
    val a = n / gcd // numărător
    val b = d / gcd // numitor

    override def toString: String = s"$a/$b"

    override def equals(obj: Any): Boolean = obj match {
      case that: RatioInt => this.a.abs == that.a.abs &&
        this.b.abs == that.b.abs &&
        this.a.sign * this.b.sign == that.a.sign * that.b.sign
      case _ => false
    }

    // TODO 2.1
    def +(other: RatioInt): RatioInt = {
      val num = BigInt(this.a) * other.b + BigInt(other.a) * this.b
      val den = BigInt(this.b) * other.b
      RatioInt(num.toInt, den.toInt)
    }

    def -(other: RatioInt): RatioInt = this + RatioInt(other.a * -1, other.b)

    def *(other: RatioInt): RatioInt = {
      val num = BigInt(this.a) * other.a
      val den = BigInt(this.b) * other.b
      RatioInt(num.toInt, den.toInt)
    }

    def /(other: RatioInt): RatioInt = this * RatioInt(other.b, other.a)

    // TODO 2.2
    def compare(other: RatioInt): Int = {
      (this.a * other.b).compare(other.a * this.b)
    }
  }
  
  // TODO 3.1
  def scaleToOne[A](l: List[(Int, A)]): List[(RatioInt, A)] = {
    val totalEl = l.foldLeft(0)((acc, el) => acc + el._1)
    l.map((nrAp, el) => (RatioInt(nrAp, totalEl), el))
  }

  // TODO 3.2
  def scaledRunLength(l: List[(Int, Bit)]): (Bit, List[RatioInt]) = {
    val totalEl = l.foldLeft(0)((acc, el) => acc + el._1)
    (l.head._2, l.map((nrAp, el) => RatioInt(nrAp, totalEl)))
  }
  
  // TODO 3.3
  def toParities(s: Str): List[Parity] = s.map(c =>
    if (c == 'G') Even
    else Odd)
  
  // TODO 3.4
  val PStrings: List[String] = List("LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG",
    "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL")
  val leftParityList: List[List[Parity]] = PStrings.map(s => toParities(s.toList))

  // TODO 3.5
  type SRL = (Bit, List[RatioInt])
  val leftOddSRL:  List[SRL] = leftOddList.map(bits => runLength[Bit](bits))
                                          .map(bitsAp => scaledRunLength(bitsAp))
  val leftEvenSRL:  List[SRL] = leftEvenList.map(bits => runLength[Bit](bits))
                                          .map(bitsAp => scaledRunLength(bitsAp))
  val rightSRL:  List[SRL] = rightList.map(bits => runLength[Bit](bits))
                                      .map(bitsAp => scaledRunLength(bitsAp))

  // TODO 4.1
  def distance(l1: SRL, l2: SRL): RatioInt = {
    if (l1._1 != l2._1) RatioInt(100, 1)
    else {
      val sl1 = l1._2
      val sl2 = l2._2

      sl1.zip(sl2).foldLeft(RatioInt(0, 1)) {
        case (acc, (el1, el2))
        => if (el1.compare(el2) > 0) acc + (el1 - el2)
        else acc + (el2 - el1)
      }
    }
  }
  
  // TODO 4.2
  def bestMatch(SRL_Codes: List[SRL], digitCode: SRL): (RatioInt, Digit) = {
    SRL_Codes.zipWithIndex
      .map((code, idx) => (distance(code, digitCode), idx)).min
  }
  
  // TODO 4.3
  def bestLeft(digitCode: SRL): (Parity, Digit) = {
    val matchEven = bestMatch(leftEvenSRL, digitCode)
    val matchOdd = bestMatch(leftOddSRL, digitCode)
    if (matchEven._1.compare(matchOdd._1) < 0) (Even, matchEven._2)
    else (Odd, matchOdd._2)
  }
  
  // TODO 4.4
  def bestRight(digitCode: SRL): (Parity, Digit) = (NoParity, bestMatch(rightSRL, digitCode)._2)

  def chunkWith[A](f: List[A] => (List[A], List[A]))(l: List[A]): List[List[A]] = {
    l match {
      case Nil => Nil
      case _ =>
        val (h, t) = f(l)
        h :: chunkWith(f)(t)
    }
  }
  
  def chunksOf[A](n: Int)(l: List[A]): List[List[A]] =
    chunkWith((l: List[A]) => l.splitAt(n))(l)

  // TODO 4.5
  def findLast12Digits(rle:  List[(Int, Bit)]): List[(Parity, Digit)] = {
    if (rle.length != 59) Nil
    else {
      val leftGroup = rle.slice(3, 27).grouped(4).toList.map(l => scaledRunLength(l)).map(srl => bestLeft(srl))
      val rightGroup = rle.slice(32, 56).grouped(4).toList.map(l => scaledRunLength(l)).map(srl => bestRight(srl))
      leftGroup ::: rightGroup
    }
  }

  // TODO 4.6
  def firstDigit(l: List[(Parity, Digit)]): Option[Digit] = {
    val paritiesList = l.take(6).map((p, d) => p)
    leftParityList.zipWithIndex.collectFirst {
      case (list, idx) if list == paritiesList => idx
    }
  }

  // TODO 4.7
  def checkDigit(l: List[Digit]): Digit = {
    val sum = l.take(12).zipWithIndex.foldLeft(0)((acc, el)  =>
      if (el._2 % 2 == 0) acc + el._1
      else acc + el._1 * 3
    )
    (10 - sum % 10) % 10
  }
  
  // TODO 4.8
  def verifyCode(code: List[(Parity, Digit)]): Option[String] = {
    if (code.length != 13) Some("Not enough digits")
    else if (firstDigit(code.drop(1)) != Some(code(0)._2)) { println(firstDigit(code));Some("Wrong parity digit")}
    else if (checkDigit(code.map((p, d) => d)) != code(12)._2) Some("Wrong control digit")
    else Some(code.map((p, d) => d).foldLeft("")((acc, digit) => acc + digit.toString))
  }
  
  // TODO 4.9
  def solve(rle:  List[(Int, Bit)]): Option[String] = {
    val code = findLast12Digits(rle)
    firstDigit(code).map(d => (NoParity, d) :: code).flatMap(verifyCode)
  }
  
  def checkRow(row: List[Pixel]): List[List[(Int, Bit)]] = {
    val rle = runLength(row);

    def condition(sl: List[(Int, Pixel)]): Boolean = {
      if (sl.isEmpty) false
      else if (sl.size < 59) false
      else sl.head._2 == 1 &&
        sl.head._1 == sl.drop(2).head._1 &&
        sl.drop(56).head._1 == sl.drop(58).head._1
    }

    rle.sliding(59, 1)
      .filter(condition)
      .toList
      .map(_.map(pair => (pair._1, toBit(pair._2))))
  }
}


