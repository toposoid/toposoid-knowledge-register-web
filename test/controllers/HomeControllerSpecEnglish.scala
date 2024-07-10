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
import com.ideal.linked.toposoid.common.{FeatureType, IMAGE, SENTENCE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.featurevector.model.{FeatureVectorIdentifier, FeatureVectorSearchResult, SingleFeatureVectorForSearch}
import com.ideal.linked.toposoid.knowledgebase.image.model.SingleImage
import com.ideal.linked.toposoid.knowledgebase.nlp.model.FeatureVector
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeSentenceSet}
import com.ideal.linked.toposoid.vectorizer.FeatureVectorizer
import org.neo4j.driver.Result
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, call, status, _}
import play.api.test.{FakeRequest, _}

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpecEnglish extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerTest with Injecting {

  val transversalState = TransversalState(username="guest")

  override def beforeAll(): Unit = {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    Neo4JAccessor.delete()
  }

  private def deleteFeatureVector(featureVectorIdentifier: FeatureVectorIdentifier, featureType: FeatureType): Unit = {
    val json: String = Json.toJson(featureVectorIdentifier).toString()
    if (featureType.equals(SENTENCE)) {
      ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "delete", transversalState)
    } else if (featureType.equals(IMAGE)) {
      ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "delete", transversalState)
    }
  }

  private def getImageVector(url: String): FeatureVector = {
    val singleImage = SingleImage(url)
    val json: String = Json.toJson(singleImage).toString()
    val featureVectorJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_COMMON_IMAGE_RECOGNITION_HOST"), conf.getString("TOPOSOID_COMMON_IMAGE_RECOGNITION_PORT"), "getFeatureVector", transversalState)
    Json.parse(featureVectorJson).as[FeatureVector]
  }

  "HomeController POST(english KnowledgeSentenceSet)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr: String =
        """{
          |	"premiseList": [
          |		{
          |			"sentence": "This is premise-1.",
          |			"lang": "en_US",
          |			"extentInfoJson": "{}",
          |     "isNegativeSentence":false,
          |     "knowledgeForImages": []
          |		},
          |		{
          |			"sentence": "This is premise-2.",
          |			"lang": "en_US",
          |			"extentInfoJson": "{}",
          |     "isNegativeSentence":false,
          |     "knowledgeForImages": []
          |		},
          |		{
          |			"sentence": "There are two cats.",
          |			"lang": "en_US",
          |			"extentInfoJson": "{}",
          |      "isNegativeSentence": false,
          |      "knowledgeForImages":[{
          |                             "id": "",
          |                             "imageReference": {
          |                               "reference": {
          |                                      "url": "",
          |                                      "surface": "cats",
          |                                      "surfaceIndex": 3,
          |                                      "isWholeSentence": false,
          |                                      "originalUrlOrReference": "http://images.cocodataset.org/val2017/000000039769.jpg"},
          |                               "x": 27,
          |                               "y": 41,
          |                               "width": 287,
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
          |			"sentence": "This is claim-1.",
          |			"lang": "en_US",
          |			"extentInfoJson": "{}",
          |     "isNegativeSentence":false,
          |     "knowledgeForImages": []
          |		},
          |		{
          |			"sentence": "This is claim-2.",
          |			"lang": "en_US",
          |			"extentInfoJson": "{}",
          |     "isNegativeSentence":false,
          |     "knowledgeForImages": []
          |		},
          |		{
          |			"sentence": "There is a dog",
          |			"lang": "en_US",
          |			"extentInfoJson": "{}",
          |      "isNegativeSentence": false,
          |      "knowledgeForImages":[{
          |                             "id": "",
          |                             "imageReference": {
          |                               "reference": {
          |                                      "url": "",
          |                                      "surface": "dog",
          |                                      "surfaceIndex": 3,
          |                                      "isWholeSentence": false,
          |                                      "originalUrlOrReference": "http://images.cocodataset.org/train2017/000000428746.jpg"},
          |                               "x": 435,
          |                               "y": 227,
          |                               "width": 91,
          |                               "height": 69
          |                               }
          |                            }]
          |		}
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
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> Json.toJson(transversalState).toString())
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.regist(), fr)
      status(result) mustBe OK

      Thread.sleep(40000)
      val query = "MATCH x=(:ClaimNode{surface:'claim-1'})-[:LocalEdge]-(:ClaimNode)-[:LocalEdge{logicType:'OR'}]-(:ClaimNode)-[:LocalEdge]-(:ClaimNode{surface:'claim-2'}) return x"
      val queryResult:Result = Neo4JAccessor.executeQueryAndReturn(query)
      assert(queryResult.hasNext())
      val query2 = "MATCH x=(:PremiseNode{surface:'premise-1'})-[:LocalEdge]-(:PremiseNode)-[:LocalEdge{logicType:'AND'}]-(:PremiseNode)-[:LocalEdge]-(:PremiseNode{surface:'premise-2'}) return x"
      val queryResult2:Result = Neo4JAccessor.executeQueryAndReturn(query2)
      assert(queryResult2.hasNext())
      val query3 = "MATCH x=(:PremiseNode{surface:'premise-1'})-[:LocalEdge]-(:PremiseNode)-[:LocalEdge{logicType:'IMP'}]-(:ClaimNode)-[:LocalEdge]-(:ClaimNode{surface:'claim-1'}) return x"
      val queryResult3:Result = Neo4JAccessor.executeQueryAndReturn(query3)
      assert(queryResult3.hasNext())


      val queryResult4: Result = Neo4JAccessor.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[:ImageEdge]->(t:PremiseNode{surface:'cats'}) RETURN s, t")
      assert(queryResult4.hasNext)
      val urlCat = queryResult4.next().get("s").get("url").asString()
      val queryResult5: Result = Neo4JAccessor.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/train2017/000000428746.jpg'})-[:ImageEdge]->(t:ClaimNode{surface:'dog'}) RETURN s, t")
      assert(queryResult5.hasNext)
      val urlDog = queryResult5.next().get("s").get("url").asString()

      val knowledgeSentenceSet:KnowledgeSentenceSet = Json.parse(jsonStr).as[KnowledgeSentenceSet]
      for(knowledge <- knowledgeSentenceSet.premiseList:::knowledgeSentenceSet.claimList){
        val vector = FeatureVectorizer.getSentenceVector(Knowledge(knowledge.sentence, "en_US", "{}"), transversalState)
        val json:String = Json.toJson(SingleFeatureVectorForSearch(vector=vector.vector, num=1)).toString()
        val featureVectorSearchResultJson:String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
        val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
        assert(result.ids.size > 0)
        result.ids.map(x => deleteFeatureVector(x, SENTENCE))

        knowledge.knowledgeForImages.foreach(x => {
          val url: String = x.imageReference.reference.surface match {
            case "cats" => urlCat
            case "dog" => urlDog
            case _ => "BAD URL"
          }
          val vector = this.getImageVector(url)
          val json: String = Json.toJson(SingleFeatureVectorForSearch(vector = vector.vector, num = 1)).toString()
          val featureVectorSearchResultJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
          val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
          assert(result.ids.size > 0 && result.similarities.head > 0.999)
          result.ids.map(x => deleteFeatureVector(x, IMAGE))
        })

      }
    }

  }
}