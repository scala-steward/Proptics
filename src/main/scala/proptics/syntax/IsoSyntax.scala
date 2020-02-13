package proptics.syntax

import cats.Functor
import cats.arrow.Profunctor
import proptics.{Iso, Optic}
import proptics.internal.{Exchange, Re}

object IsoSyntax {
  implicit class IsoReOps[P[_, _], S, T, A, B](val iso: Optic[Re[P, A, B, *, *], S, T, A, B]) extends AnyVal {
    def re: Optic[P, B, A, T, S] = Optic(iso(Re(identity[P[B, A]])).runRe)
  }

  implicit class AnIsoOps[P[_, _], S, T, A, B](val iso: Optic[Exchange[A, B, *, *], S, T, A, B]) extends AnyVal {
    def withIso[R](f: (S => A) => (B => T) => R): R = {
      val exchange = iso(Exchange(identity, identity))

      f(exchange.get)(exchange.inverseGet)
    }

    def au[E](f: (B => T) => E => S): E => A = withIso(sa => bt => e => sa(f(bt)(e)))

    def auf[E, R](f: P[R, A] => E => B)(g: P[R, S])(implicit ev: Profunctor[P]): E => T =
      withIso(sa => bt => e => bt(f(ev.rmap(g)(sa))(e)))

    def under(f: T => S): B => A = withIso(sa => bt => sa compose f compose bt)

    def mapping[F[_], G[_]](implicit ev: Functor[F], ev1: Functor[G]): Iso[F[S], G[T], F[A], G[B]] =
      withIso(sa => bt => Iso.iso(ev.lift(sa))(ev1.lift(bt)))

    def dimapping[Q[_, _], SS, TT, AA, BB](iso2: Optic[Exchange[AA, BB, *, *], SS, TT, AA, BB])(implicit ev0: Profunctor[P], ev1: Profunctor[Q]): Iso[P[A, SS], Q[B, TT], P[S, AA], Q[T, BB]] =
      withIso(sa => bt => iso2.withIso(ssaa => bbtt => {
        Iso.iso[P[A, SS], Q[B, TT], P[S, AA], Q[T, BB]](ev0.dimap(_)(sa)(ssaa))(ev1.dimap(_)(bt)(bbtt))
      }))
  }
}