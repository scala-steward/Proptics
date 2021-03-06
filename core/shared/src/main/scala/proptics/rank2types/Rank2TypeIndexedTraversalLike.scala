package proptics.rank2types

import proptics.internal.Indexed
import proptics.profunctor.Wander

private[proptics] trait Rank2TypeIndexedTraversalLike[I, S, T, A, B] {
  def apply[P[_, _]](indexed: Indexed[P, I, A, B])(implicit ev: Wander[P]): P[S, T]
}
