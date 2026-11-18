# Changelog

All notable changes to this library are documented in this file. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.3.0] - Unreleased

## [0.2.0] - 2026-11-18

This release contains breaking changes. Until version 1.0, the API may still change between minor releases.

### Breaking changes

- `Bip21URI.otherParameters` is now a non-nullable `List<OtherParameter>` that defaults to an empty list.
- `Bip21URI.fromUri` now throws only `InvalidURIException`. It used to also throw `IllegalArgumentException` for some invalid URIs. `InvalidURIException` now takes an optional `cause`, which holds the underlying exception when there is one.
- `Amount.toBitcoin()` and `Amount.encode()` now always write exactly 8 decimal places (`0.00100000` instead of `0.001`).
- Removed `Amount(sat: String)`. Use `Amount(sat: Long)` instead.
- Removed `String.fromBitcoinIntoAmount()`. Use `Amount.decode()` instead.
- Removed `Long.satoshi()`. Use `Amount(sat)` instead.
- Removed `Label.decodeFrom()`, `Message.decodeFrom()` and `PayJoin.decodeFrom()`. `Bip21URI.fromUri` decodes values for you.
- `Label`, `Message`, `Lightning`, `PayJoin` and `OtherParameter` reject empty values. `OtherParameter` also rejects empty keys and the keys that `Bip21URI` handles itself (`amount`, `label`, `message`, `lightning`, `pj` and `pjos`), and `Bip21URI` rejects duplicate keys in `otherParameters`.
- `Bip21URI` rejects addresses that are empty or contain characters other than ASCII letters and digits.

### Changed

- The library no longer has any dependencies other than the Kotlin standard library. Ktor, `bignum` and `uri-kmp` were replaced by internal code.
- The library is built with Kotlin 2.4.20 and targets Java 17. Projects using it need Kotlin 2.3 or newer.
- Parsing is more lenient: characters that should have been percent-encoded, such as spaces, are accepted as they are. Raw `#` and control characters are still rejected.
- The URI scheme is case-insensitive when parsing, so `BITCOIN:` is accepted.
- Parameter keys and values are percent-encoded following RFC 3986: every character except `A-Z a-z 0-9 - . _ ~` is encoded.

### Fixed

- Keys and values are percent-decoded exactly once, after splitting the query. Previously, encoded `&` or `=` characters inside a value broke parsing, and some values were decoded twice.
- `toURI()` no longer drops the `lightning` parameter when it is the only parameter, and no longer crashes when `otherParameters` is an empty list.
- URIs containing a required (`req-`) parameter are rejected, as BIP-21 requires.
- Amounts are validated against the BIP-21 grammar: only digits and at most one `.` are accepted, so other number formats (signs, exponents) are rejected.
- URIs with a fragment (`#`) are rejected instead of silently losing everything after the `#`.

## 0.1.0 - 2025-06-20

Initial release.

[0.2.0]: https://github.com/kotlin-bitcoin-tools/bip21/releases/tag/v0.2.0
