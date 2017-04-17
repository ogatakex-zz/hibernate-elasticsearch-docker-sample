package sample;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.elasticsearch.ElasticsearchQueries;
import org.hibernate.search.query.engine.spi.QueryDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TweetIT {

	private SessionFactory sessionFactory;

	private Session session;

	@Before
	public void before() {
		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
				.applySetting(AvailableSettings.URL, "jdbc:h2:./db/sample")
				.applySetting(AvailableSettings.USER, "sa")
				.applySetting(AvailableSettings.PASS, "sa")
				.applySetting(AvailableSettings.DRIVER, "org.h2.Driver")
				.applySetting(AvailableSettings.DIALECT, H2Dialect.class)
				.applySetting(AvailableSettings.HBM2DDL_AUTO, "update")
				.applySetting(AvailableSettings.SHOW_SQL, true)
				.applySetting(AvailableSettings.FORMAT_SQL, true)
				.applySetting("hibernate.search.default.indexmanager", "elasticsearch")
				.build();

		Metadata metadata = new MetadataSources(standardRegistry)
				.addAnnotatedClass(Tweet.class)
				.getMetadataBuilder()
				.applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE)
				.build();

		SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
		sessionFactory = sessionFactoryBuilder.build();
		session = sessionFactory.openSession();

		session.getTransaction().begin();

		Tweet tweet1 = Tweet.builder().content("絶対は絶対にない").build();
		Tweet tweet2 = Tweet.builder().content("努力だ。勉強だ。それが天才だ").build();
		Tweet tweet3 = Tweet.builder().content("ゆっくりと急げ").build();

		session.save(tweet1);
		session.save(tweet2);
		session.save(tweet3);

		session.getTransaction().commit();
	}

	@After
	public void after() {
		session.getTransaction().begin();

		session.createQuery("delete from Tweet").executeUpdate();

		session.getTransaction().commit();

		session.close();
		sessionFactory.close();
	}

	@Test
	public void test() {
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		QueryDescriptor query = ElasticsearchQueries.fromQueryString("content:絶対");
		List<Tweet> result = fullTextSession.createFullTextQuery(query, Tweet.class).list();

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("絶対は絶対にない", result.get(0).getContent());
	}
}
