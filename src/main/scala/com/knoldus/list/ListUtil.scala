package com.knoldus.list

object ListUtil {

  def split[A](xs: List[A], n: Int): List[List[A]] = {
    if (n == 0) { xs :: Nil }
    else if (xs.size <= n) { xs :: Nil }
    else { (xs take n) :: split(xs drop n, n) }
  }

  def getSeqAsOption[T](list: Seq[T]): Option[Seq[T]] = {
    if (list.isEmpty) { None }
    else { Some(list) }
  }

  def getVectorAsOption[T](list: Vector[T]): Option[Vector[T]] = {
    if (list.isEmpty) { None }
    else { Some(list) }
  }
}
