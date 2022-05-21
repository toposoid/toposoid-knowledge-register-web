# toposoid-knowledge-register-web
This is a WEB API that works as a microservice within the toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This Microservice registers the results of predicate argument structure analysis of Japanese natural sentences in a graph database.

[![Unit Test And Build Image Action](https://github.com/toposoid/toposoid-knowledge-register-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/toposoid-knowledge-register-web/actions/workflows/action.yml)

* input

| Japanse | English |
| ------------- | ------------- |
| <img width="417" alt="" src="https://user-images.githubusercontent.com/82787843/169640988-9045f53b-2e92-4ade-9efc-c4de9c91827a.png"> | <img width="446" alt="" src="https://user-images.githubusercontent.com/82787843/169640983-23f71563-7df0-452a-9f9d-e5096465c2f3.png">
|
This is simple.

of

| Japanse | English |
| ------------- | ------------- |
| <img width="439" alt="" src="https://user-images.githubusercontent.com/82787843/169641450-8aa73417-76ce-4c72-9811-fa6f0274ddfd.png"> | <img width="438" alt="" src="https://user-images.githubusercontent.com/82787843/169641466-3f14bed7-d107-4f23-8b5f-3bec34ba7a9d.png"> |
If you want to specify Premise and Claim respectively, this may be good.

* result
<img width="1755" alt="スクリーンショット 2022-01-08 19 31 10" src="https://user-images.githubusercontent.com/82787843/148676414-0e1b0f57-0ed4-4c59-9ecc-66eb07f9bcb8.png">





## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

## Memory requirements
* Required: at least 8GB of RAM (The maximum heap memory size of the JVM is set to 6G (Application: 4G, Neo4J: 2G))
* Required: 10G or higher　of HDD

## Setup
```bash
docker-compose up -d
```
It takes more than 20 minutes to pull the Docker image for the first time.
## Usage
```bash
#Japanese
curl -X POST -H "Content-Type: application/json" -d '
{"knowledgeList":[{"sentence":"案ずるより産むが易し", "lang": "ja_JP", "extentInfoJson":"{}", "isNegativeSentence": false}]}
' http://localhost:9002/regist
#English
curl -X POST -H "Content-Type: application/json" -d '
{"knowledgeList":[{"sentence":"Our life is our art.", "lang": "en_US", "extentInfoJson":"{}, "isNegativeSentence": false"}]}
' http://localhost:9002/regist
```
Try accessing http://localhost:7474 in your browser.
You will be able to see the data you registered from the API.
as follows
<img width="1755" alt="スクリーンショット 2022-01-08 19 31 10" src="https://user-images.githubusercontent.com/82787843/148676414-0e1b0f57-0ed4-4c59-9ecc-66eb07f9bcb8.png">


## Note
* This microservice uses 9002 as the default port.
* If you want to run in a remote environment or a virtual environment, change PRIVATE_IP_ADDRESS in docker-compose.yml according to your environment.
* The memory allocated to Neo4J can be adjusted with NEO4J_dbms_memory_heap_max__size in docker-compose.yml.  
* The Node's Information In A Graph Database

| name | content(Japanse) |content(English)|
| ------------- | ------------- | ------------- |
| nodeId | 文章の文節を識別するID | ID that identifies the morpheme in the text |
| propositionId | 命題としての文章集合を識別するID |　Same as Japanese |
| currentId | 文章の何番目の文節かを識別するID | ID of token parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) |
| parentId | 当該の文節が係っている文節のID | ID of token.head parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　|
| isMainSection | 文末を表すフラグ ture/false | Same as Japanese |
| surface | 文節の表層 |token.text parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　 |
| normalizedName | 文節の正規化表現（KNPのfeatureの正規化代表表記参照） |token.lemma parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　|
| dependType　| 親の文節との関係　依存関係:D、並列関係:P | - |
| caseType | 文節の格情報（KNPのfeatureの係参照） | [Stanford Dependencies](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf)|
| namedEntity | 固有表現（KNPのfeatureのNE参照） | Mainly NER calculated from [spacy's](https://spacy.io/) [en_core_web_lg](https://spacy.io/models/en#en_core_web_lg) model |
| rangeExpressions | 範囲表現 | Same as Japanese |
| categories | カテゴリ（KNPのfeatureのカテゴリ参照） | - |
| domains | ドメイン（KNPのfeatureのドメイン参照） | - |
| isDenialWord | 否定表現を表すフラグ true/false (KNPのfeatureの否定参照) |　Negatives analyzed by [Core NLP's Depedency](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf)　|
| isConditionalConnection | 条件節及びそれに類する節を表すフラグ（KNPのfeatureの条件節候補参照） | Conditional clause analyzed by [Depedency of CoreNLP](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf) |
| normalizedNameYomi　| 文節の正規化表現の読み仮名 | - |
| surfaceYomi | 文節の表層の読み仮名 | - |
| modalityType | モダリティ（KNPのfeatureのモダリティ参照） | - |
| logicType | (KNPの並列タイプ参照、その他包含関係はIMPという種別もあり) | - |
| nodeType | com.ideal.linked.toposoid.common.SentenceType |　Same as Japanese |
| extentText | 拡張領域 |Same as Japanese |

The Edge's Information In A Graph Database
| name | content(Japanese) | content(English) |
| ------------- | ------------- | ------------- |
| sourceId | 文節間の関係で係受けの子を識別するID | ID of token parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) |
| destinationId　| 文節間の関係で係受けの親を識別するID | ID of token.head parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　|
| caseStr　| 文節間の関係（格構造 etc） | [Stanford Dependencies](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf) |
| dependType | KnowledgeBaseNodeのdependType参照 | - |
| logicType　| KnowledgeBaseNodeのlogicType参照 | - |

## License
toposoid/toposoid-knowledge-register-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
