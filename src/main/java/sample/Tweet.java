package sample;

import lombok.*;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.elasticsearch.analyzer.ElasticsearchTokenizerFactory;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Indexed
@AnalyzerDef(
		name = "japanese",
		tokenizer = @TokenizerDef(
				factory = ElasticsearchTokenizerFactory.class,
				params = {
						@Parameter(name = "type", value = "'kuromoji_tokenizer'")
				}
		)
)
@Analyzer(definition = "japanese")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tweet implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Field
	private String content;

	@Override
	public String toString() {
		return content;
	}
}