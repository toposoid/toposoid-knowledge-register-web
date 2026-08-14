/*
 * Copyright (C) 2025  Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.{FeatureType, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
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
import controllers.TestUtilsEx.uploadImage
import controllers.TestUtilsEx.uploadTable
import com.ideal.linked.toposoid.knowledgebase.regist.model.KnowledgeForTable
import com.ideal.linked.toposoid.knowledgebase.regist.model.TableReference
import controllers.TestUtilsEx.deleteFeatureVector
import controllers.TestUtilsEx.getImageVector

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpecJapanese extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerTest with Injecting {

  val transversalState = TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")

  override def beforeAll(): Unit = {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    TestUtilsEx.deleteNeo4JAllData(transversalState)
  }


  "HomeController POST(japanese KnowledgeSentenceSet)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]

      val knowledge1 = Knowledge(sentence = "これはテストの前提1です。", lang = "", extentInfoJson = "{}")
      val knowledge2 = Knowledge(sentence = "これはテストの前提2です。", lang = "", extentInfoJson = "{}")
      val reference3 = Reference(url = "", surface = "猫が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg", metaInformations = List.empty[String])
      val imageReference3 = ImageReference(reference = reference3, x = 27, y = 41, width = 287, height = 435)
      val knowledgeForImages3 = uploadImage(KnowledgeForImage(id = "", imageReference = imageReference3), transversalState)
      val knowledge3 = Knowledge(sentence = "猫が２匹います。", lang = "", extentInfoJson = "{}", knowledgeForImages = List(knowledgeForImages3))

      val reference3a= Reference(url = "", surface = "データが", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "https://www.e-stat.go.jp/stat-search/file-download?statInfId=000001086170&fileKind=0", metaInformations = List.empty[String])
      val tableReference3a = TableReference(reference = reference3a, skipHeaderRows= 5, multiHeaderRowsForExcel=4, sheetNameForExcel="se0101")
      val knowledgeForTable3a = uploadTable(KnowledgeForTable(id = "", tableReference = tableReference3a), transversalState)
      val knowledge3a = Knowledge(sentence = "データがあります。", lang = "ja_JP", extentInfoJson = "{}", knowledgeForTables=List(knowledgeForTable3a))


      val knowledge4 = Knowledge(sentence = "これはテストの主張1です。", lang = "", extentInfoJson = "{}")
      val knowledge5 = Knowledge(sentence = "これはテストの主張2です。", lang = "", extentInfoJson = "{}")
      val reference6 = Reference(url = "", surface = "犬が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/train2017/000000428746.jpg", metaInformations = List.empty[String])
      val imageReference6 = ImageReference(reference = reference6, x = 435, y = 227, width = 91, height = 69)
      val knowledgeForImages6 = uploadImage(KnowledgeForImage(id = "", imageReference = imageReference6), transversalState)
      val knowledge6 = Knowledge(sentence = "犬が1匹います。", lang = "", extentInfoJson = "{}", knowledgeForImages = List(knowledgeForImages6))

      val reference7 = Reference(url = "", surface = "証拠が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "https://www.e-stat.go.jp/stat-search/file-download?statInfId=000001086171&fileKind=0", metaInformations = List.empty[String])
      val tableReference7 = TableReference(reference = reference7, skipHeaderRows= 8, multiHeaderRowsForExcel=4, sheetNameForExcel="se0102")
      val knowledgeForTable7 = uploadTable(KnowledgeForTable(id = "", tableReference = tableReference7), transversalState)
      val knowledge7 = Knowledge(sentence = "証拠があります。", lang = "ja_JP", extentInfoJson = "{}", knowledgeForTables = List(knowledgeForTable7))


      val knowledgeSentenceSet: KnowledgeSentenceSet = KnowledgeSentenceSet(
        premiseList = List(knowledge1, knowledge2, knowledge3, knowledge3a),
        premiseLogicRelation = List(PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 1), PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 2), PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 3)),
        claimList = List(knowledge4, knowledge5, knowledge6),
        claimLogicRelation = List(PropositionRelation(operator = "OR", sourceIndex = 0, destinationIndex = 1), PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 2), PropositionRelation(operator = "AND", sourceIndex = 0, destinationIndex = 3))
      )
      val jsonStr = Json.toJson(knowledgeSentenceSet).toString()

      val fr = FakeRequest(POST, "/registerForManual")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> Json.toJson(transversalState).toString())
        .withJsonBody(Json.parse(jsonStr))
      val result= call(controller.registerForManual(), fr)
      status(result) mustBe OK
      Thread.sleep(60000)
      val query = "MATCH x=(:ClaimNode{surface:'主張２です。'})<-[:LocalEdge{logicType:'OR'}]-(:ClaimNode{surface:'主張１です。'})<-[:LocalEdge{logicType:'IMP'}]-(:PremiseNode{surface:'前提１です。'})-[:LocalEdge{logicType:'AND'}]->(:PremiseNode{surface:'前提２です。'}) return x"
      val queryResult:Neo4jRecords = TestUtilsEx.executeQueryAndReturn(query, transversalState)
      assert(queryResult.records.size == 1)
      val result2: Neo4jRecords = TestUtilsEx.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[:ImageEdge]->(t:PremiseNode{surface:'猫が'}) RETURN s, t", transversalState)
      assert(result2.records.size == 1)
      val urlCat = result2.records.head.head.value.featureNode.get.url
      val result3: Neo4jRecords = TestUtilsEx.executeQueryAndReturn("MATCH (s:ImageNode{source:'http://images.cocodataset.org/train2017/000000428746.jpg'})-[:ImageEdge]->(t:ClaimNode{surface:'犬が'}) RETURN s, t", transversalState)
      assert(result3.records.size == 1)
      val urlDog = result3.records.head.head.value.featureNode.get.url


      val result5: Neo4jRecords = TestUtilsEx.executeQueryAndReturn("MATCH (s:TableNode{source:'https://www.e-stat.go.jp/stat-search/file-download?statInfId=000001086170&fileKind=0'})-[:TableEdge]->(t:PremiseNode{surface:'データが'}) RETURN s, t", transversalState)
      val urlTable1 = result5.records.head.head.value.featureNode.get.url
      assert(result5.records.size == 1)
      val result6: Neo4jRecords = TestUtilsEx.executeQueryAndReturn("MATCH (s:TableNode{source:'https://www.e-stat.go.jp/stat-search/file-download?statInfId=000001086171&fileKind=0'})-[:TableEdge]->(t:ClaimNode{surface:'証拠が'}) RETURN s, t", transversalState)
      val urlTable2 = result6.records.head.head.value.featureNode.get.url
      assert(result6.records.size == 1)

      val knowledgeSentenceSet2:KnowledgeSentenceSet = Json.parse(jsonStr).as[KnowledgeSentenceSet]

      for(knowledge <- knowledgeSentenceSet2.premiseList:::knowledgeSentenceSet2.claimList){
        val vector = FeatureVectorizer.getSentenceVector(Knowledge(knowledge.sentence, "ja_JP", "{}"), transversalState)
        val json:String = Json.toJson(SingleFeatureVectorForSearch(vector=vector.vector, num=1)).toString()
        val featureVectorSearchResultJson:String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
        val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
        assert(result.ids.size > 0 && result.similarities.head > 0.999)
        result.ids.map(x => deleteFeatureVector(x, FeatureType.SENTENCE, transversalState))

        knowledge.knowledgeForImages.foreach(x => {
          val url:String = x.imageReference.reference.surface match {
            case "猫が" => urlCat
            case "犬が" => urlDog
            case _ => "BAD URL"
          }
          val vector = getImageVector(url, transversalState)
          val json: String = Json.toJson(SingleFeatureVectorForSearch(vector = vector.vector, num = 1)).toString()
          val featureVectorSearchResultJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
          val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
          assert(result.ids.size > 0 && result.similarities.head > 0.999)
          result.ids.map(x => deleteFeatureVector(x, FeatureType.IMAGE, transversalState))
        })

        knowledge.knowledgeForTables.foreach(x => {
          val url: String = x.tableReference.reference.surface match {
            case "データが" => urlTable1
            case "証拠が" => urlTable2
            case _ => "BAD URL"
          }
          val vector = TestUtilsEx.getTableVector(url, transversalState)
          val json: String = Json.toJson(SingleFeatureVectorForSearch(vector = vector.vector, num = 1)).toString()
          val featureVectorSearchResultJson: String = ToposoidUtils.callComponent(json, conf.getString("TOPOSOID_TABLE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_TABLE_VECTORDB_ACCESSOR_PORT"), "search", transversalState)
          val result = Json.parse(featureVectorSearchResultJson).as[FeatureVectorSearchResult]
          assert(result.ids.size > 0 && result.similarities.filter(x => x > 0.95).size > 0)
          result.ids.map(x => TestUtilsEx.deleteFeatureVector(x, FeatureType.TABLE, transversalState))
        })

      }

    }
  }

  "HomeController POST(split)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr: String =
        """{
          |    "sentence": "富士山は、2013年に世界遺産に登録された。"
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/split")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> Json.toJson(transversalState).toString())
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.split(), fr)
      status(result) mustBe OK
      val jsonResult = contentAsJson(result).toString()
      val correctJson = """[{"surface":"富士山は、","index":0},{"surface":"２０１３年に","index":1},{"surface":"世界遺産に","index":2},{"surface":"登録された。","index":3}]"""
      assert(Json.parse(jsonResult) == Json.parse(correctJson))
    }
  }

}

