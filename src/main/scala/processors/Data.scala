package processors

import edu.arizona.sista.processors.fastnlp.FastNLPProcessor
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{write, writePretty}

case class Sentence(
              words: List[String],
              lemmas: List[String],
              tags:List[String],
              entities: List[String],
              dependencies: List[Map[String, Any]]
                     )

// storage class (mirrors the Document class in Processors) for the annotated text.
// this will be dumped to json
case class Document(text: String, sentences: Map[Int, Sentence])

object TextProcessor {
  // initialize a processor
  val proc = new FastNLPProcessor()
  // convert processors document to a json-serializable format
  def toAnnotatedDocument(text: String): Document = {
    val doc = proc.annotate(text)
    val sentencePairs =
      for {(s, i) <- doc.sentences.zipWithIndex
           words = s.words
           lemmas = s.lemmas.get
           tags = s.tags.get
           entities = s.entities.get
           // reformatting the dependencies as a List of maps results in a more readable json output
           deps = s.dependencies.get.allEdges().map {
             case (i:Int, o:Int, rel: String) => Map("incoming"->i,"outgoing" -> o, "relation" -> rel)
           }
      } yield (i + 1, Sentence(words.toList, lemmas.toList, tags.toList, entities.toList, deps))
    Document(text, sentencePairs.toMap)
  }

  def textToJSON(text: String): JValue = {
    val doc = toAnnotatedDocument(text)
    implicit val formats = Serialization.formats(NoTypeHints)
    val json = write(doc)
    json
  }
}