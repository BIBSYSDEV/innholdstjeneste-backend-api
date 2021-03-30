# innholdstjeneste-backend-api
A common api for backend of contentsservice (aka. innholdsinformasjontjeneste)

### Dataformat
Dataformat for utveksling med API. https://api.sandbox.bibs.aws.unit.no/contents


```
{"contents": {
"isbn": "9788215027227",
"title": "Skriv så det selger! : en bok for deg som vil overtale og overbevise",
"author": "Calvert, Christine",
"date_of_publication": "2018",
"description_short": "",
"description_long": "",
"table_of_contents": "",
"image_small": "/9788215027227/small/9788215027227.jpg",
"image_large": "",
"image_original": "",
"audio_file": "",
"source": "NIELSEN",
"modified": "";
"created":"someTimestamp"
}
}
```


Har en book flere ISBNer blir det en innslag i basen for hver ISBN.

ISBN er en trimmet string (ikke noe - ).

source skulle være noen bestemte verdier som NIELSEN, BOKBASE, ALMA, ADABAS, BIBSYS.

### Endepunkter

Under https://api.sandbox.bibs.aws.unit.no/contents finnes 3 endepunkter:

_GET_ - hente contents data med gitt isbn (`?isbn=9788215027227`)

_POST_ - legge til nye contents data i basen (feiler, da isbn finnes fra før)

_PUT_ - legge til eller oppdatere contents data (isbn, source er obligatoriske felt og kan ikke oppdateres).

