/*
 * Copyright 2021 Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.common.{FeatureType, IMAGE, SENTENCE, ToposoidUtils}
import com.ideal.linked.toposoid.knowledgebase.featurevector.model.{FeatureVectorId, FeatureVectorIdentifier, FeatureVectorSearchResult, SingleFeatureVectorForSearch}
import com.ideal.linked.toposoid.knowledgebase.image.model.SingleImage
import com.ideal.linked.toposoid.knowledgebase.nlp.model.{FeatureVector, SingleSentence}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeSentenceSet}
import com.ideal.linked.toposoid.vectorizer.FeatureVectorizer
import org.neo4j.driver.{Record, Result}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.duration.Duration

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpecJapanese extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerTest with Injecting {

  override def beforeAll(): Unit = {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema")
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema")
    Neo4JAccessor.delete()
  }

  private def deleteFeatureVector(featureVectorIdentifier: FeatureVectorIdentifier, featureType: FeatureType):Unit = {
    val json: String = Json.toJson(featureVectorIdentifier).toString()
    if(featureType.equals(SENTENCE)){
      ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "delete")
    }else if(featureType.equals(IMAGE)){
      ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "delete")
    }
  }

  private def getImageVector(url: String): FeatureVector = {
    val singleImage = SingleImage(url)
    val json: String = Json.toJson(singleImage).toString()
    val featureVectorJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_COMMON_IMAGE_RECOGNITION_HOST"), conf.getString("TOPOSOID_COMMON_IMAGE_RECOGNITION_PORT"), "getFeatureVector")
    Json.parse(featureVectorJson).as[FeatureVector]
  }

  "HomeController POST(japanese KnowledgeSentenceSet)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr:String = """{
                             |	"premiseList": [
                             |		{
                             |			"sentence": "これはテストの前提1です。",
                             |			"lang": "ja_JP",
                             |			"extentInfoJson": "{}",
                             |      "isNegativeSentence": false,
                             |      "knowledgeForImages": []
                             |		},
                             |		{
                             |			"sentence": "これはテストの前提2です。",
                             |			"lang": "ja_JP",
                             |			"extentInfoJson": "{}",
                             |      "isNegativeSentence": false,
                             |      "knowledgeForImages": []
                             |		},
                             |		{
                             |			"sentence": "猫が２匹います。",
                             |			"lang": "ja_JP",
                             |			"extentInfoJson": "{}",
                             |      "isNegativeSentence": false,
                             |      "knowledgeForImages":[{
                             |                             "id": "",
                             |                             "imageReference": {
                             |                               "reference": {
                             |                                      "url": "",
                             |                                      "surface": "猫が",
                             |                                      "surfaceIndex": 0,
                             |                                      "isWholeSentence": false,
                             |                                      "originalUrlOrReference": "http://images.cocodataset.org/val2017/000000039769.jpg"},
                             |                               "x": 27,
                             |                               "y": 41,
                             |                               "weight": 287,
                             |                               "height": 435
                             |                               }
                             |                            }]
                             |		}
                             |	],
                             |	"premiseLogicRelation": [
                             |		{
                             |			"operator": "AND",
                             |			"sourceIndex": 0,
                             |			"destinationIndex": 1
                             |		}
                             |	],
                             |	"claimList": [
                             |		{
                             |			"sentence": "これはテストの主張1です。",
                             |			"lang": "ja_JP",
                             |			"extentInfoJson": "{}",
                             |      "isNegativeSentence": false,
                             |      "knowledgeForImages": []
                             |		},
                             |		{
                             |			"sentence": "これはテストの主張2です。",
                             |			"lang": "ja_JP",
                             |			"extentInfoJson": "{}",
                             |      "isNegativeSentence": false,
                             |      "knowledgeForImages": []
                             |		},
                             |		{
                             |			"sentence": "犬が1匹います。",
                             |			"lang": "ja_JP",
                             |			"extentInfoJson": "{}",
                             |      "isNegativeSentence": false,
                             |      "knowledgeForImages":[{
                             |                             "id": "",
                             |                             "imageReference": {
                             |                               "reference": {
                             |                                      "url": "",
                             |                                      "surface": "犬が",
                             |                                      "surfaceIndex": 0,
                             |                                      "isWholeSentence": false,
                             |                                      "originalUrlOrReference": "http://images.cocodataset.org/train2017/000000428746.jpg"},
                             |                               "x": 435,
                             |                               "y": 227,
                             |                               "weight": 91,
                             |                               "height": 69
                             |                               }
                             |                            }]
                             |		}
                             |
                             |	],
                             |	"claimLogicRelation": [
                             |		{
                             |			"operator": "OR",
                             |			"sourceIndex": 0,
                             |			"destinationIndex": 1
                             |		}
                             |	]
                             |}""".stripMargin
      val fr = FakeRequest(POST, "/regist")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result= call(controller.regist(), fr)
      status(result) mustBe OK
      Thread.sleep(30000)
      val query = "MATCH x=(:ClaimNode{surface:'主張２です。'})<-[:LocalEdge{logicType:'OR'}]-(:ClaimNode{surface:'主張１です。'})<-[:LocalEdge{logicType:'IMP'}]-(:PremiseNode{surface:'前提１です。'})-[:LocalEdge{logicType:'AND'}]->(:PremiseNode{surface:'前提２です。'}) return x"
      val queryResult:Result = Neo4JAccessor.executeQueryAndReturn(query)
      assert(queryResult.hasNext())
      val result2: Result = Neo4JAccessor.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[:ImageEdge]->(t:PremiseNode{surface:'猫が'}) RETURN s, t")
      assert(result2.hasNext)
      val urlCat = result2.next().get("s").get("url").asString()
      val result3: Result = Neo4JAccessor.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/train2017/000000428746.jpg'})-[:ImageEdge]->(t:ClaimNode{surface:'犬が'}) RETURN s, t")
      assert(result3.hasNext)
      val urlDog = result3.next().get("s").get("url").asString()

      val knowledgeSentenceSet:KnowledgeSentenceSet = Json.parse(jsonStr).as[KnowledgeSentenceSet]

      for(knowledge <- knowledgeSentenceSet.premiseList:::knowledgeSentenceSet.claimList){
        val vector = FeatureVectorizer.getSentenceVector(Knowledge(knowledge.sentence, "ja_JP", "{}"))
        val json:String = Json.toJson(SingleFeatureVectorForSearch(vector=vector.vector, num=1)).toString()
        val featureVectorSearchResultJson:String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "search")
        val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
        assert(result.ids.size > 0 && result.similarities.head > 0.999)
        result.ids.map(x => deleteFeatureVector(x, SENTENCE))

        knowledge.knowledgeForImages.foreach(x => {
          val url:String = x.imageReference.reference.surface match {
            case "猫が" => urlCat
            case "犬が" => urlDog
            case _ => "BAD URL"
          }
          val vector = this.getImageVector(url)
          val json: String = Json.toJson(SingleFeatureVectorForSearch(vector = vector.vector, num = 1)).toString()
          val featureVectorSearchResultJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "search")
          val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
          assert(result.ids.size > 0 && result.similarities.head > 0.999)
          result.ids.map(x => deleteFeatureVector(x, IMAGE))
        })
      }
    }
  }

}
