# Using the bip21 library

The library is centered around the `Bip21URI` type, which offers two main functions: 

- `toURI(): String`
- `fromURI(input: String): Bip21URI`

The `Bip21URI` type is a data class with typesafe fields for each of the standard BIP-21 URI parameters: `Amount`, `Label`, `Message`, `Lightning`, and other unknown parameters (`OtherParameter`).
