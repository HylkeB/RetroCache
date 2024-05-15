# RetroCache
_**Work in progress**_

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.hylkeb/retrocache)

RetroCache is a library on top of Retrofit2, which helps with caching of requests and responses.
It exposes its data as a finite state machine, to be observed by the consumer of this library.
It has 3 very straight forward methods:
* `fun observeData(): Flow<RequestState<R>>`
* `suspend fun getData(forceRefresh: Boolean): Result<R>`
* `fun forceRefresh()`

The finite state machine that is created per request looks like this:

[![](https://mermaid.ink/img/pako:eNp9UrtuwzAM_BWBoxEvHT10Sv0BzVh1YCXaFupHQklFgiD_XlkPxGmLapF4vCNPlK6gFk3QgHXoaG-wZ5zqryc5i7DeqndR18-iJacGM_cJLVFMvTAvLJoEig7NSPoP2h4dBlbcxgV1ISX5tkdgVaInt1KlnCvRLazolTomOyRRrPJDEzE6Hw2X0vFGCb8mZHulg1eKrL0nMhCTuVtxE2t8kMKJ1qojRV_t6ktwoob95Mm60nxdmyqbjsVrFv4jyGEbZxpkOX4Y8kaWeb_8P_hMqhvsYCKe0Ojw9HE6EtxAE0lowlEjf0qQ88pD75bDZVbQOPa0A3_U958CTYejpds3Snu4oQ?type=png)](https://mermaid.live/edit#pako:eNp9UrtuwzAM_BWBoxEvHT10Sv0BzVh1YCXaFupHQklFgiD_XlkPxGmLapF4vCNPlK6gFk3QgHXoaG-wZ5zqryc5i7DeqndR18-iJacGM_cJLVFMvTAvLJoEig7NSPoP2h4dBlbcxgV1ISX5tkdgVaInt1KlnCvRLazolTomOyRRrPJDEzE6Hw2X0vFGCb8mZHulg1eKrL0nMhCTuVtxE2t8kMKJ1qojRV_t6ktwoob95Mm60nxdmyqbjsVrFv4jyGEbZxpkOX4Y8kaWeb_8P_hMqhvsYCKe0Ojw9HE6EtxAE0lowlEjf0qQ88pD75bDZVbQOPa0A3_U958CTYejpds3Snu4oQ)

