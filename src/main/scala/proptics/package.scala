package object proptics {
  /**
   * [[Lens_]] is a specialization of [[Lens]]. An optic of type [[Lens_]]
   * can change only the value of its focus, not its type.
   */
  type Lens_[S, A] = Lens[S, S, A, A]

  /** [[Prism_]] is a specialization of [[Prism]]. An optic of type [[Prism_]] */
  type Prism_[S, A] = Prism[S, S, A, A]

  /** [[Iso_]] is a specialization of [[Iso]]. An optic of type [[Iso_]] */
  type Iso_[S, A] = Iso[S, S, A, A]

  /** [[Traversal_]] is a specialization of [[Traversal]]. An optic type [[Traversal_]] */
  type Traversal_[S, A] = Traversal[S, S, A, A]

  /** [[ATraversal_]] is a specialization of [[ATraversal]]. An optic type [[ATraversal_]] */
  type ATraversal_ [S, A] = ATraversal[S, S, A, A]

  /** [[Optic_]] is a specialization of [[Optic]] */
  type Optic_[P[_, _], S, A] = Optic[P, S, S, A, A]

  /** [[AnIso_]] is a specialization of [[AnIso]]. An optic type [[AnIso_]] */
  type AnIso_[S, A] = AnIso[S, A, A, A]

  /** [[ALens_]] is a specialization of [[ALens]]. An optic type [[ALens_]] */
  type ALens_[S, A] = ALens[S, A, A, A]

  /** [[AnIndexedLens_]] is a specialization of [[AnIndexedLens]]. An optic type [[AnIndexedLens_]] */
  type AnIndexedLens_[I, S, A] = AnIndexedLens[I, S, S, A, A]

  /** [[APrism_]] is a specialization of [[APrism]]. An optic type [[APrism_]] */
  type APrism_[S, A] = APrism[S, S, A, A]

  /** [[Grate_]] is a specialization of [[Grate]]. An optic type [[Grate_]] */
  type Grate_[P[_, _], S, A] = Grate[P, S, S, A, A]

  /** [[AGrate_]] is a specialization of [[AGrate]]. An optic type [[AGrate_]] */
  type AGrate_[S, A] = AGrate[S, S, A, A]

  /** [[Getter_]] is a specialization of [[Getter]]. An optic type [[Getter_]] */
  type Getter_[R, S, A] = Getter[R, S, S, A, A]

  /** [[AGetter_]] is a specialization of [[AGetter]]. An optic type [[AGetter_]] */
  type AGetter_[S, A] = AGetter[S, S, A, A]

  /** [[Setter_]] is a specialization of [[Setter]]. An optic type [[Setter_]] */
  type Setter_[S, A] = Setter[S, S, A, A]

  /** [[Review_]] is a specialization of [[Review]]. An optic type [[Review_]] */
  type Review_[S, A] = Review[S, S, A, A]

  /** [[IndexedTraversal_]] is a specialization of [[IndexedTraversal]]. An optic type [[IndexedTraversal_]] */
  type IndexedTraversal_[P[_, _], I, S, A] = IndexedTraversal[P, I, S, S, A, A]

  /** [[IndexedFold_]] is a specialization of [[IndexedFold]]. An optic type [[IndexedFold_]] */
  type IndexedFold_[R, I, S, A] = IndexedFold[R, I, S, S, A, A]

  /** [[IndexedGetter_]] is a specialization of [[IndexedGetter]]. An optic type [[IndexedGetter_]] */
  type IndexedGetter_[I, S, A] = IndexedGetter[I, S, S, A, A]

  /** [[IndexedSetter_]] is a specialization of [[IndexedSetter]]. An optic type [[IndexedSetter_]] */
  type IndexedSetter_[I, S, A] = IndexedSetter[I, S, S, A, A]
}