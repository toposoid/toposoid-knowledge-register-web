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

import actors.RegistKnowledgeActor
import actors.RegistKnowledgeActor.{RegistKnowledgeUsingSentenceActor, RegistKnowledgeUsingSentenceSetActor}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeSentenceSet}
import com.typesafe.scalalogging.LazyLogging

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

case class KnowledgeSentences(knowledgeList:List[Knowledge])
object KnowledgeSentences {
  implicit val jsonWrites = Json.writes[KnowledgeSentences]
  implicit val jsonReads = Json.reads[KnowledgeSentences]
}

/**
 * This module is a WEB API for converting sentences into a knowledge graph and registering them in a graph database.
 * @param system
 * @param cc
 * @param ec
 */
@Singleton
class HomeController @Inject()(system: ActorSystem, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) with LazyLogging{

  val knowledgeRegistActor = system.actorOf(RegistKnowledgeActor.props, "knowledge-regist-actor")
  implicit val timeout: Timeout = 60.seconds

  /**
   * With json as input, the process of registering to the graph database is executed asynchronously.
   * If the execution is successful, it returns a response at that point.
   * @return
   */
/*
  @deprecated
  def regist()  = Action(parse.json) { request =>

    try{
      val json = request.body
      val knowledgeSentences: KnowledgeSentences = Json.parse(json.toString).as[KnowledgeSentences]
      (knowledgeRegistActor ? RegistKnowledgeUsingSentenceActor(knowledgeSentences.knowledgeList))
      Ok({"\"result\":\"OK\""}).as(JSON)
    }catch{
      case e: Exception => {
        logger.error(e.toString(), e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }
*/
  def regist()  = Action(parse.json) { request =>
    try{
      val json = request.body
      val knowledgeSentenceSet: KnowledgeSentenceSet = Json.parse(json.toString).as[KnowledgeSentenceSet]

      (knowledgeRegistActor ? RegistKnowledgeUsingSentenceSetActor(knowledgeSentenceSet))
      Ok({"\"result\":\"OK\""}).as(JSON)
    }catch{
      case e: Exception => {
        logger.error(e.toString(), e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }

}
