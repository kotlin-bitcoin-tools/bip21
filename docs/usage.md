# Using the bip21 library

The library is centered around the [`Bip21URI`](./api/bip21/org.bitcointools.bip21/-bip21-u-r-i/index.html) type, which offers two main functions: 

- `toURI(): String`
- `fromURI(input: String): Bip21URI`

The `Bip21URI` type is a data class with typesafe fields for each of the standard BIP-21 URI parameters: `Amount`, `Label`, `Message`, `Lightning`, and other unknown parameters (`OtherParameter`).

For more examples on how to use the library, take a look at the [test suite] as well as the [samples in the API documentation].

[`Bip21URI`]: ./api/bip21/org.bitcointools.bip21/-bip21-u-r-i/index.html
[test suite]: https://github.com/kotlin-bitcoin-tools/bip21/tree/master/src/commonTest/kotlin/org/bitcointools/bip21
[samples in the API documentation]: ./api/bip21/org.bitcointools.bip21/-bip21-u-r-i/index.html
