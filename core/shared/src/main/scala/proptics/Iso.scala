package proptics

import cats.arrow.{Profunctor, Strong}
import cats.instances.function._
import cats.syntax.eq._
import cats.syntax.option._
import cats.syntax.either._
import cats.{Applicative, Comonad, Eq, Monoid}
import proptics.internal._
import proptics.profunctor.{Choice, Closed, Costar}
import proptics.rank2types._
import proptics.syntax.FunctionSyntax._

import scala.Function.const
import scala.{Function => F}

/**
  * A generalized isomorphism
  *
  * @tparam S the source of an [[Iso_]]
  * @tparam T the modified source of an [[Iso_]]
  * @tparam A the target of an [[Iso_]]
  * @tparam B the modified target of a [[Iso_]]
  */
abstract class Iso_[S, T, A, B] extends Serializable {
  self =>
  private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Profunctor[P]): P[S, T]

  def view[R](s: S): A = self[Forget[A, *, *]](Forget(identity[A])).runForget(s)

  def set(b: B): S => T = over(const(b))

  def over(f: A => B): S => T = self(f)

  def overF[F[_]: Applicative](f: A => F[B])(s: S): F[T] = traverse(s)(f)

  def traverse[F[_]](s: S)(f: A => F[B])(implicit ev: Applicative[F]): F[T] = ev.map(f(self.view(s)))(self.set(_)(s))

  def filter(f: A => Boolean): S => Option[A] = s => view(s).some.filter(f)

  def exists(f: A => Boolean): S => Boolean = f compose view

  def noExists(f: A => Boolean): S => Boolean = s => !exists(f)(s)

  def contains(s: S)(a: A)(implicit ev: Eq[A]): Boolean = exists(_ === a)(s)

  def notContains(s: S)(a: A)(implicit ev: Eq[A]): Boolean = !contains(s)(a)

  def zipWith[F[_]](f: A => A => B): S => S => T = self(Zipping(f))(Zipping.closedZipping).runZipping

  def zipWithF[F[_]: Comonad](fs: F[S])(f: F[A] => B)(implicit ev: Applicative[F]): T =
    self(Costar(f))(Costar.profunctorCostar[F](ev)).runCostar(fs)

  def re: Iso_[B, A, T, S] = new Iso_[B, A, T, S] {
    override def apply[P[_, _]](pab: P[T, S])(implicit ev: Profunctor[P]): P[B, A] =
      self(Re(identity[P[B, A]])).runRe(pab)
  }

  def asLens_ : Lens_[S, T, A, B] = new Lens_[S, T, A, B] {
    override private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Strong[P]): P[S, T] = self(pab)
  }

  def compose[C, D](other: Iso_[A, B, C, D]): Iso_[S, T, C, D] = new Iso_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Profunctor[P]): P[S, T] = self(other(pab))
  }

  def compose[C, D](other: AnIso_[A, B, C, D]): AnIso_[S, T, C, D] = new AnIso_[S, T, C, D] {
    override private[proptics] def apply(exchange: Exchange[C, D, C, D]) = self(other(exchange))
  }

  def compose[C, D](other: Lens_[A, B, C, D]): Lens_[S, T, C, D] = new Lens_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Strong[P]): P[S, T] = self(other(pab))
  }

  def compose[C, D](other: ALens_[A, B, C, D]): ALens_[S, T, C, D] = new ALens_[S, T, C, D] {
    override def apply(shop: Shop[C, D, C, D]): Shop[C, D, S, T] = self(other(shop))
  }

  def compose[C, D](other: Prism_[A, B, C, D]): Prism_[S, T, C, D] = new Prism_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Choice[P]): P[S, T] = self(other(pab))
  }

  def compose[C, D](other: APrism_[A, B, C, D]): APrism_[S, T, C, D] = new APrism_[S, T, C, D] {
    override private[proptics] def apply(market: Market[C, D, C, D]) = self(other(market))

    override def traverse[F[_]](s: S)(f: C => F[D])(implicit ev: Applicative[F]): F[T] = {
      val market = self(other(Market[C, D, C, D](identity, _.asRight[D])))

      market.from(s).fold(ev.pure, c => ev.map(f(c))(market.to))
    }
  }

  def compose[C, D](other: Traversal_[A, B, C, D]): Traversal_[S, T, C, D] = new Traversal_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Wander[P]): P[S, T] = self(other(pab))
  }

  def compose[C, D](other: ATraversal_[A, B, C, D]): ATraversal_[S, T, C, D] =
    ATraversal_(new RunBazaar[* => *, C, D, S, T] {
      override def apply[F[_]](pafb: C => F[D])(s: S)(implicit ev: Applicative[F]): F[T] = {
        val bazaar = other(new Bazaar[* => *, C, D, C, D] {
          override def runBazaar: RunBazaar[* => *, C, D, C, D] = new RunBazaar[* => *, C, D, C, D] {
            override def apply[G[_]](pafb: C => G[D])(s: C)(implicit ev: Applicative[G]): G[D] = pafb(s)
          }
        })

        self(bazaar).runBazaar(pafb)(s)
      }
    })

  def compose[C, D](other: Setter_[A, B, C, D]): Setter_[S, T, C, D] = new Setter_[S, T, C, D] {
    override private[proptics] def apply(pab: C => D) = self(other(pab))
  }

  def compose[C, D](other: Review_[A, B, C, D]): Review_[S, T, C, D] = new Review_[S, T, C, D] {
    override private[proptics] def apply(tagged: Tagged[C, D]) = self(other(tagged))(Tagged.choiceTagged)
  }

  def compose[C, D](other: Grate_[A, B, C, D]): Grate_[S, T, C, D] = new Grate_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Closed[P]): P[S, T] = self(other(pab))
  }
  def compose[C, D](other: AGrate_[A, B, C, D]): AGrate_[S, T, C, D] = new AGrate_[S, T, C, D] {
    override def apply(grating: Grating[C, D, C, D]): Grating[C, D, S, T] = self(other(grating))
  }

  def compose[C, D](other: Getter_[A, B, C, D]): Getter_[S, T, C, D] = new Getter_[S, T, C, D] {
    override def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] = self(other(forget))(Forget.wanderForget)
  }

  def compose[C, D](other: AGetter_[A, B, C, D]): AGetter_[S, T, C, D] = new AGetter_[S, T, C, D] {
    override private[proptics] def apply(forget: Forget[C, C, D]) = self(other(forget))

    override protected def foldMap[R](s: S)(f: C => R)(implicit ev: Monoid[R]): R = {
      val forget = other(Forget(identity))

      f(forget.runForget(self.view(s)))
    }
  }

  def compose[C, D](other: Fold_[A, B, C, D]): Fold_[S, T, C, D] = new Fold_[S, T, C, D] {
    override def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] = self(other(forget))(Forget.wanderForget)
  }
}

object Iso_ {
  private[proptics] def apply[S, T, A, B](f: Rank2TypeIsoLike[S, T, A, B]): Iso_[S, T, A, B] = new Iso_[S, T, A, B] {
    override def apply[P[_, _]](pab: P[A, B])(implicit ev: Profunctor[P]): P[S, T] = f(pab)
  }

  def apply[S, T, A, B](get: S => A)(inverseGet: B => T): Iso_[S, T, A, B] = iso(get)(inverseGet)

  def iso[S, T, A, B](get: S => A)(inverseGet: B => T): Iso_[S, T, A, B] =
    Iso_(new Rank2TypeIsoLike[S, T, A, B] {
      override def apply[P[_, _]](pab: P[A, B])(implicit ev: Profunctor[P]): P[S, T] = ev.dimap(pab)(get)(inverseGet)
    })

  def curried[A, B, C, D, E, F]: Iso_[(A, B) => C, (D, E) => F, A => B => C, D => E => F] =
    iso[(A, B) => C, (D, E) => F, A => B => C, D => E => F](_.curried)(F.uncurried[D, E, F])

  def uncurried[A, B, C, D, E, F]: Iso_[A => B => C, D => E => F, (A, B) => C, (D, E) => F] =
    iso[A => B => C, D => E => F, (A, B) => C, (D, E) => F](F.uncurried[A, B, C])(_.curried)

  def flipped[A, B, C, D, E, F]: Iso_[A => B => C, D => E => F, B => A => C, E => D => F] =
    iso[A => B => C, D => E => F, B => A => C, E => D => F](_.flip)(_.flip)
}

object Iso {
  private[proptics] def apply[S, A](f: Rank2TypeIsoLike[S, S, A, A]): Iso[S, A] = new Iso[S, A] {
    override def apply[P[_, _]](pab: P[A, A])(implicit ev: Profunctor[P]): P[S, S] = f(pab)
  }

  def apply[S, A](get: S => A)(inverseGet: A => S): Iso[S, A] = Iso_.iso(get)(inverseGet)

  def iso[S, A](get: S => A)(inverseGet: A => S): Iso[S, A] = Iso_.iso(get)(inverseGet)

  /** If `A1` is obtained from `A` by removing a single value, then `Option[A1]` is isomorphic to `A` */
  def non[A](a: A)(implicit ev: Eq[A]): Iso[Option[A], A] = {
    def g(a1: A): Option[A] = if (a1 === a) None else a.some

    Iso_.iso((op: Option[A]) => op.getOrElse(a))(g)
  }
}
