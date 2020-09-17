---
id: iso
title: Iso
---

An `Iso` enables you to transform back and forth between two types without losing information.<br/>
`Iso[S, A]` means that `S` and `A` are isomorphic – the two types represent the same information.<br/>
Iso is useful when you need to convert between types, a simple example would be, transform a `String` into a `List[Char]`.

## Constructing Isos

Isos are constructed using the [Iso[S, A]#apply](/Proptics/api/proptics/Iso$.html) function. For a given `Iso[S, A]` it takes two conversion functions as arguments,
`view` which produces an `A` given an `S`, and `review` which produces an `S` given an `A`.

```scala
object Iso {
  def apply[S, A](view: S => A)(review: A => S): Iso[S, A]
}
```

```scala
import proptics.Iso
// import proptics.Iso

val isoStringToList = Iso[String, List[Char]](_.toList)(_.mkString)
// isoStringToList: proptics.Iso[String,List[Char]] = proptics.Iso_$$anon$16@4b898027  
```

## Common functions of an Iso

#### view
```scala
isoStringToList.view("Proptics") 
// res0: List[Char] = List(P, r, o, p, t, i, c, s)
```

#### review
```scala
isoStringToList.review(chars)
// res1: String = Proptics
```

#### exists
```scala
isoStringToList.exists(_.length === 8)("Proptics")
// res2: Boolean = true
```

#### contains
```scala
isoStringToList.contains(_.contains(80))("Proptics")
// res3: Boolean = true
```

#### find
```scala
isoStringToList.find(_.contains(80))("Proptics")
// res4: Option[List[Char]] = Some(List(P, r, o, p, t, i, c, s))
```

## Iso internal encoding

`Iso[S, A]` is the monomorphic short notation version (does not change the type of the structure) of the polymorphic one `Iso_[S, T, A, B]`

```scala
type Iso[S, A] = Iso_[S, S, A, A]
``` 

`Iso_[S, T, A, B]` is basically a function `P[A, B] => P[S, T]` that takes a [Profunctor](/Proptics/docs/profunctors/profunctor) of P[_, _].

```scala
abstract class Iso_[S, T, A, B] extends Serializable {
  private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Profunctor[P]): P[S, T]
}
```

So for an `Iso[S, A] ~ Iso[S, S, A, A]` the `apply` method will be `P[A, A] => P[S, S]`. <br/> 
As you recall, in order to construct an `Iso[S, A]` we need two functions:<br/> 
- `S => A`<br/>
- `A => S`<br/>

If we feed those function to the `dimap` method of a profunctor, we will end up with the desired result

```scala
// feeding the first `S => A` function as a contravariant argument will get us
  P[A, A] => P[S, A]

//feeding the second `A => S` function as a covariant argument will get us
  P[S, A] => P[S, S]

// The expected end result
  P[A, A] => P[S, S] 
```

## Laws

An Iso must satisfy all [IsoLaws](/Proptics/api/proptics/law/IsoLaws.html). These laws reside in the [proptics.law](/Proptics/api/proptics/law/index.html) package.<br/>
All laws constructed from the reversibility law, which says that we can completely reverse the transformation.<br/>
```scala
def sourceReversibility(s: S): IsEq[S]
 
def focusReversibility(a: A): IsEq[A]
```