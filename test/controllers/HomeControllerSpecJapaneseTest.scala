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
import com.ideal.linked.toposoid.common.{FeatureType, IMAGE, SENTENCE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.featurevector.model.{FeatureVectorId, FeatureVectorIdentifier, FeatureVectorSearchResult, SingleFeatureVectorForSearch}
import com.ideal.linked.toposoid.knowledgebase.image.model.SingleImage
import com.ideal.linked.toposoid.knowledgebase.nlp.model.{FeatureVector, SingleSentence}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{ImageReference, Knowledge, KnowledgeForImage, KnowledgeSentenceSet, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.neo4j.Neo4jRecords
import com.ideal.linked.toposoid.vectorizer.FeatureVectorizer
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
class HomeControllerSpecJapaneseTest extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerTest with Injecting {

  val transversalState = TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")

  override def beforeAll(): Unit = {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    TestUtils.deleteNeo4JAllData(transversalState)
  }

  private def deleteFeatureVector(featureVectorIdentifier: FeatureVectorIdentifier, featureType: FeatureType):Unit = {
    val json: String = Json.toJson(featureVectorIdentifier).toString()
    if(featureType.equals(SENTENCE)){
      ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "delete", transversalState)
    }else if(featureType.equals(IMAGE)){
      ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "delete", transversalState)
    }
  }

  private def getImageVector(url: String): FeatureVector = {
    val singleImage = SingleImage(url)
    val json: String = Json.toJson(singleImage).toString()
    val featureVectorJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_COMMON_IMAGE_RECOGNITION_HOST"), conf.getString("TOPOSOID_COMMON_IMAGE_RECOGNITION_PORT"), "getFeatureVector", transversalState)
    Json.parse(featureVectorJson).as[FeatureVector]
  }

  "HomeController POST(japanese KnowledgeSentenceSet)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]

      val knowledge1 = Knowledge(sentence = "これはテストの前提1です。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge2 = Knowledge(sentence = "これはテストの前提2です。", lang = "ja_JP", extentInfoJson = "{}")
      val reference3 = Reference(url = "", surface = "猫が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg", metaInformations = List.empty[String])
      val imageReference3 = ImageReference(reference = reference3, x = 27, y = 41, width = 287, height = 435)
      val knowledgeForImages3 = KnowledgeForImage(id = "", imageReference = imageReference3)
      val knowledge3 = Knowledge(sentence = "猫が２匹います。", lang = "ja_JP", extentInfoJson = "{}", knowledgeForImages = List(knowledgeForImages3))

      val knowledge4 = Knowledge(sentence = "これはテストの主張1です。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge5 = Knowledge(sentence = "これはテストの主張2です。", lang = "ja_JP", extentInfoJson = "{}")
      val reference6 = Reference(url = "", surface = "犬が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/train2017/000000428746.jpg", metaInformations = List.empty[String])
      val imageReference6 = ImageReference(reference = reference6, x = 435, y = 227, width = 91, height = 69)
      val knowledgeForImages6 = KnowledgeForImage(id = "", imageReference = imageReference6)
      val knowledge6 = Knowledge(sentence = "犬が1匹います。", lang = "ja_JP", extentInfoJson = "{}", knowledgeForImages = List(knowledgeForImages6))

      val knowledgeSentenceSet: KnowledgeSentenceSet = KnowledgeSentenceSet(
        premiseList = List(knowledge1, knowledge2, knowledge3),
        premiseLogicRelation = List(PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 1), PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 2)),
        claimList = List(knowledge4, knowledge5, knowledge6),
        claimLogicRelation = List(PropositionRelation(operator = "OR", sourceIndex = 0, destinationIndex = 1), PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 2))
      )
      val jsonStr = Json.toJson(knowledgeSentenceSet).toString()

      val fr = FakeRequest(POST, "/registerForManual")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> Json.toJson(transversalState).toString())
        .withJsonBody(Json.parse(jsonStr))
      val result= call(controller.registerForManual(), fr)
      status(result) mustBe OK
      Thread.sleep(60000)
      val query = "MATCH x=(:ClaimNode{surface:'主張２です。'})<-[:LocalEdge{logicType:'OR'}]-(:ClaimNode{surface:'主張１です。'})<-[:LocalEdge{logicType:'IMP'}]-(:PremiseNode{surface:'前提１です。'})-[:LocalEdge{logicType:'AND'}]->(:PremiseNode{surface:'前提２です。'}) return x"
      val queryResult:Neo4jRecords = TestUtils.executeQueryAndReturn(query, transversalState)
      assert(queryResult.records.size == 1)
      val result2: Neo4jRecords = TestUtils.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[:ImageEdge]->(t:PremiseNode{surface:'猫が'}) RETURN s, t", transversalState)
      assert(result2.records.size == 1)
      val urlCat = result2.records.head.head.value.featureNode.get.url
      val result3: Neo4jRecords = TestUtils.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/train2017/000000428746.jpg'})-[:ImageEdge]->(t:ClaimNode{surface:'犬が'}) RETURN s, t", transversalState)
      assert(result3.records.size == 1)
      val urlDog = result3.records.head.head.value.featureNode.get.url

      val knowledgeSentenceSet2:KnowledgeSentenceSet = Json.parse(jsonStr).as[KnowledgeSentenceSet]

      for(knowledge <- knowledgeSentenceSet2.premiseList:::knowledgeSentenceSet2.claimList){
        val vector = FeatureVectorizer.getSentenceVector(Knowledge(knowledge.sentence, "ja_JP", "{}"), transversalState)
        val json:String = Json.toJson(SingleFeatureVectorForSearch(vector=vector.vector, num=1)).toString()
        val featureVectorSearchResultJson:String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
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
          val featureVectorSearchResultJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
          val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
          assert(result.ids.size > 0 && result.similarities.head > 0.999)
          result.ids.map(x => deleteFeatureVector(x, IMAGE))
        })
      }

    }
  }

}

