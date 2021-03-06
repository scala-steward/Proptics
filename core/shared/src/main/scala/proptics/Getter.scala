package proptics

import cats.data.State
import cats.syntax.eq._
import cats.syntax.option._
import cats.{Eq, Monoid}

import proptics.internal.{Forget, Indexed}

/** A [[Getter_]] is a [[Fold]] without a [[Monoid]]
  * <p>
  * [[Getter_]] is just any get function (S -> A)
  * </p>
  * @tparam S the source of a [[Getter_]]
  * @tparam T the modified source of a [[Getter_]]
  * @tparam A the focus of a [[Getter_]]
  * @tparam B the modified focus of a [[Getter_]]
  */
abstract class Getter_[S, T, A, B] extends Serializable { self =>
  private[proptics] def apply(forget: Forget[A, A, B]): Forget[A, S, T]

  /** view the focus of a [[Getter_]] */
  def view(s: S): A = self(Forget(identity)).runForget(s)

  /** test whether a predicate holds for the focus of a [[Getter_]] */
  def exists(f: A => Boolean): S => Boolean = f compose view

  /** test whether a predicate does not hold for the focus of a [[Getter_]] */
  def notExists(f: A => Boolean): S => Boolean = !exists(f)(_)

  /** test whether a [[Getter_]] contains a specific focus */
  def contains(a: A)(s: S)(implicit ev: Eq[A]): Boolean = exists(_ === a)(s)

  /** test whether a [[Getter_]] does not contain a specific focus */
  def notContains(a: A)(s: S)(implicit ev: Eq[A]): Boolean = !contains(a)(s)

  /** find if the focus of a [[Getter_]] is satisfying a predicate. */
  def find(f: A => Boolean): S => Option[A] = s => view(s).some.find(f)

  /** view the focus of a [[Getter_]] in the state of a monad */
  def use(implicit ev: State[S, A]): State[S, A] = ev.inspect(view)

  /** compose a [[Getter_]] with a function lifted to a [[Getter_]] */
  def to[C, D](f: A => C): Getter_[S, T, C, D] = compose(Getter_[A, B, C, D](f))

  /** transform a [[Getter_]] to a [[Fold_]] */
  def asFold: Fold_[S, T, A, B] = new Fold_[S, T, A, B] {
    override private[proptics] def apply[R: Monoid](forget: Forget[R, A, B]): Forget[R, S, T] =
      Forget(forget.runForget compose self.view)
  }

  /** compose a [[Getter_]] with an [[Iso_]] */
  def compose[C, D](other: Iso_[A, B, C, D]): Getter_[S, T, C, D] = new Getter_[S, T, C, D] {
    override private[proptics] def apply(forget: Forget[C, C, D]): Forget[C, S, T] =
      Forget(forget.runForget compose other.view compose self.view)
  }

  /** compose a [[Getter_]] with an [[AnIso_]] */
  def compose[C, D](other: AnIso_[A, B, C, D]): Getter_[S, T, C, D] = self compose other.asIso

  /** compose a [[Getter_]] with a [[Lens_]] */
  def compose[C, D](other: Lens_[A, B, C, D]): Getter_[S, T, C, D] = new Getter_[S, T, C, D] {
    override private[proptics] def apply(forget: Forget[C, C, D]): Forget[C, S, T] =
      Forget(forget.runForget compose other.view compose self.view)
  }

  /** compose a [[Getter_]] with an [[ALens_]] */
  def compose[C, D](other: ALens_[A, B, C, D]): Getter_[S, T, C, D] = self compose other.asLens

  /** compose a [[Getter_]] with a [[Prism_]] */
  def compose[C, D](other: Prism_[A, B, C, D]): Fold_[S, T, C, D] = new Fold_[S, T, C, D] {
    override private[proptics] def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] =
      Forget(s => other.preview(self.view(s)).fold(Monoid[R].empty)(forget.runForget))
  }

  /** compose a [[Getter_]] with an [[APrism_]] */
  def compose[C, D](other: APrism_[A, B, C, D]): Fold_[S, T, C, D] = self compose other.asPrism

  /** compose a [[Getter_]] with an [[AffineTraversal_]] */
  def compose[C, D](other: AffineTraversal_[A, B, C, D]): Fold_[S, T, C, D] = new Fold_[S, T, C, D] {
    override private[proptics] def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] =
      Forget(s => other.preview(self.view(s)).fold(Monoid[R].empty)(forget.runForget))
  }

  /** compose a [[Getter_]] with an [[AnAffineTraversal_]] */
  def compose[C, D](other: AnAffineTraversal_[A, B, C, D]): Fold_[S, T, C, D] = new Fold_[S, T, C, D] {
    override private[proptics] def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] =
      Forget(s => other.preview(self.view(s)).fold(Monoid[R].empty)(forget.runForget))
  }

  /** compose a [[Getter_]] with a [[Traversal_]] */
  def compose[C, D](other: Traversal_[A, B, C, D]): Fold_[S, T, C, D] = new Fold_[S, T, C, D] {
    override private[proptics] def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] =
      Forget(s => other.foldMap(self.view(s))(forget.runForget))
  }

  /** compose a [[Getter_]] with an [[ATraversal_]] */
  def compose[C, D](other: ATraversal_[A, B, C, D]): Fold_[S, T, C, D] = self compose other.asTraversal

  /** compose a [[Getter_]] with a [[Getter_]] */
  def compose[C, D](other: Getter_[A, B, C, D]): Getter_[S, T, C, D] = new Getter_[S, T, C, D] {
    override private[proptics] def apply(forget: Forget[C, C, D]): Forget[C, S, T] =
      Forget(forget.runForget compose other.view compose self.view)
  }

  /** compose a [[Getter_]] with a [[Fold_]] */
  def compose[C, D](other: Fold_[A, B, C, D]): Fold_[S, T, C, D] = new Fold_[S, T, C, D] {
    override private[proptics] def apply[R: Monoid](forget: Forget[R, C, D]): Forget[R, S, T] =
      Forget(s => other.foldMap(self.view(s))(forget.runForget))
  }

  /** compose a [[Getter_]] with an [[IndexedLens_]] */
  def compose[I, C, D](other: IndexedLens_[I, A, B, C, D]): IndexedGetter_[I, S, T, C, D] = new IndexedGetter_[I, S, T, C, D] {
    private[proptics] def apply(indexed: Indexed[Forget[(C, I), *, *], I, C, D]): Forget[(C, I), S, T] =
      Forget(s => indexed.runIndex.runForget(other.view(self.view(s))))
  }

  /** compose a [[Getter_]] with an [[AnIndexedLens_]] */
  def compose[I, C, D](other: AnIndexedLens_[I, A, B, C, D]): IndexedGetter_[I, S, T, C, D] = new IndexedGetter_[I, S, T, C, D] {
    private[proptics] def apply(indexed: Indexed[Forget[(C, I), *, *], I, C, D]): Forget[(C, I), S, T] =
      Forget(s => indexed.runIndex.runForget(other.view(self.view(s))))
  }

  /** compose a [[Getter_]] with an [[IndexedTraversal_]] */
  def compose[I, C, D](other: IndexedTraversal_[I, A, B, C, D]): IndexedFold_[I, S, T, C, D] = new IndexedFold_[I, S, T, C, D] {
    override private[proptics] def apply[R: Monoid](indexed: Indexed[Forget[R, *, *], I, C, D]): Forget[R, S, T] =
      Forget(s => other.foldMap(self.view(s))(indexed.runIndex.runForget))
  }

  /** compose an [[Getter_]] with an [[IndexedGetter_]] */
  def compose[I, C, D](other: IndexedGetter_[I, A, B, C, D]): IndexedFold_[I, S, T, C, D] = new IndexedFold_[I, S, T, C, D] {
    override private[proptics] def apply[R: Monoid](indexed: Indexed[Forget[R, *, *], I, C, D]): Forget[R, S, T] =
      Forget(indexed.runIndex.runForget compose other.view compose self.view)
  }

  /** compose an [[Getter_]] with an [[IndexedFold_]] */
  def compose[I, C, D](other: IndexedFold_[I, A, B, C, D]): IndexedFold_[I, S, T, C, D] = new IndexedFold_[I, S, T, C, D] {
    override private[proptics] def apply[R: Monoid](indexed: Indexed[Forget[R, *, *], I, C, D]): Forget[R, S, T] =
      Forget(s => other.foldMap(self.view(s))(indexed.runIndex.runForget))
  }
}

object Getter_ {
  /** create a polymorphic [[Getter_]] from a [[Getter_.apply]] function */
  private[Getter_] def apply[S, T, A, B](f: Forget[A, A, B] => Forget[A, S, T])(implicit ev: DummyImplicit): Getter_[S, T, A, B] =
    new Getter_[S, T, A, B] {
      override def apply(forget: Forget[A, A, B]): Forget[A, S, T] = f(forget)
    }

  /** create a polymorphic [[Getter_]] from a getter function */
  def apply[S, T, A, B](f: S => A): Getter_[S, T, A, B] =
    Getter_((forget: Forget[A, A, B]) => Forget[A, S, T](forget.runForget compose f))

  /** polymorphic identity of a [[Getter_]] */
  def id[S, T]: Getter_[S, T, S, T] = Getter_[S, T, S, T](identity[S] _)
}

object Getter {
  /** create a monomorphic [[Getter]] from a getter function */
  def apply[S, A](f: S => A): Getter[S, A] = Getter_(f)

  /** polymorphic identity of a [[Getter]] */
  def id[S]: Getter[S, S] = Getter_.id[S, S]
}
