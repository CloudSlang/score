package org.score.samples;


import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import org.apache.log4j.Logger;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.OOActionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 7/28/2014
 *
 * @author Bonczidai Levente
 */
public class OpenstackApplications {
	private final static Logger logger = Logger.getLogger(OpenstackApplications.class);
	private ApplicationContext context;

	@SuppressWarnings("unused")
	@Autowired
	private Score score;

	@SuppressWarnings("unused")
	@Autowired
	private EventBus eventBus;

	public static void main(String[] args) {
		OpenstackApplications app = loadApp();
		app.registerEventListeners();
		app.start();
	}

	@SuppressWarnings("unchecked")
	private void start() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", 1L)); // how will we know the key
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 2L));


		builder.addStep(0L, "org.score.content.httpclient.HttpClientAction", "execute", navigationMatchers);

		navigationMatchers = new ArrayList<>(); // doesnt work if using the same reference
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "result", "400", 2L));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 2L));

		builder.addStep(1L, "org.score.samples.openstack.actions.HttpClientSendEmailMock", "sendEmail", navigationMatchers);

		builder.addFinalStep(2L, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");

		ExecutionPlan executionPlan = builder.getExecutionPlan();

		Map<String, Serializable> executionContext = new HashMap<>();
		prepareExecutionContext(executionContext);

		score.trigger(executionPlan, executionContext);
	}

	private void prepareExecutionContext(Map<String, Serializable> executionContext) {

		//http post
		executionContext.put("url", "http://16.59.58.200:8774/v2/1ef9a1495c774e969ad6de86e6f025d7/servers");
		executionContext.put("method", "post");
		executionContext.put("headers", "X-AUTH-TOKEN: PKIZ_eJy1WF13osoSfe9fcd9nzRpAScIj0oBw7PagCHa_CUagAU1iBOHX3wJNYnIyk497T7KMK3w0Vbtq797Fz5_wMzJth_7HIPPun5-IOI7n1cz9a8edtIqp7sXGDo5RbBgm1g3dM4-Gr49HyeIBJ8SwhWHgu9w25vf23IkGCHvmWF_Bwd3OTJIr-7ZxU6bQIh6QbJqNrp3trFopwcERuwTO3cUDKvNwtmFLD87rGSJCP1JDqqfwoYFXT_GupngnkWaoEJFLRMTZxNAzvkzr1dIt4sa5cgynIdiUJr4-RPBHDrCuToUuTYXXhpmzdwz3jhvdhW4d2ZbgYVpF9qyIT-ckHqpptPX6CJFTwgmFNqvlSFqFWh9qtA32kVEnThkcWOjuedhfLMXboOiiWYWn8EkYlGgamkeKc5m1kJ29UKkoUo55zv1E4T4vCXZkjk9pRKX1yOd9ZDkP5crZyhrqTsRK0KzLQvAFTde2tY-UuHtiBKAVUTmrI6U4rMfk6VjKbfkuKoK5D2kiWG23Hs_qaXZTEZ8dqJ_DJzkAuPVUJAPaetVaOFUX7nvRot-FG5fB9gkTp3ALrhRVlGsHSKEHOdoCdqWaosg6hfLdSNBHwD0j7o8yUjKZl_DrJw21ZykXqYAUEokprCViVBCFCiKSUwrjIIvsQgSBS_o0ylRaj_WrSXNzpNlQns7hYwwbRLB-NcUAiqEpJLs58pCpzDelqR8I2sYS93NlGlo5hX6ZYl7QktVEBDltXG05rxPUNQsfj6pVqEqxrO3h-6FvV-tx3UWyHhf1ufYiUuR6vZwVTtY1GXRjuMhQl19Uagqbu9rEAPJcLnhaCI5ZeRQWh_fSQZf5PKeTfT4ddMqnr23Dw_VdpAx71BfnPvAV9RwydKMdNNDO-8tI0HvIfiUS9BrZSyo7NfFjlfiFIKV3ZG0OC3gSK82GAMWpH9dU8SREWlPhJ57Xa6DuKqTBIq_7NFbjmRTjXTWBayZiocJnOBGwMN4NIRJlMmANmrRWwaETie-pNCTQsgCQDyGHnsLtxZCWC4ULvaGhNwD69qUFQcljW-v40KKlcryLyse2AyWUpV5xbpejogdpy6toHDzyJWsua99Fx4DiHEBFQJBsAx31Ge6_Rzj0T8aRf5PO5BVDactkBGANqW-lLOQZxUFB2rSgLcgb9lri5w1RTJUq5LcM_XMjdQw9s-53pEN_Yt1TyBdqfIgGPO2OPwGPvoP8CzakhU78GupvQUe_W_lp4WfEMRlOsSlTXz8yxS2ozQYEJ0OIwAKRdKB13Zy1HpBH_7om9s_7AO13wIZ7nH23M8EGKp97n7TOFvgw3r8iTM-TwT5j4ewRFnzFVvQbuqrgDU4E2p4Bzfqnzk-qpf4dlYs-VdSFypddFMfP6MDLwmWRd5EhR5g1aV0AMkip7wrmz1Iauhlog8rESBAMLW7PSlIGJ23cjmRWHu-Y_KndOW-mPtA96HXigu50AxU6rMBHoa77evS3MzW2T2KxCuWUK4u3bbwFu3NgcPxSP9D32zgBTPQKFMn8N1r5tPrT4i9bPDly3xxykUjU98AjOEMwWc6A44U67ZDvFBoEhgvv0-2MXgREr0knIPOvtTTqBcTWZG7zKi5lcGl91-1ftXiZFp21fU-90TfQB4FlHT7Vl43me5VCnynVnyJBT6F8JZJnlRIJOBSclkTAhOB7LSzQcNsVcENOhCsohqcIXSU2--a-0BmM-ZcMxpdlTQEyHeVuy37DPAE35Xz58c6NPl97Uj2PBUtSeb0-DjP0vzCxexj61NOUY7UOvdfMxPGR-mQAIM5yjsFmYxhjwHh3bpyAMyXCyjufBOX9IzPR70pIGq0FtVbAtcGop-GecMY_q4Q-KFPBWudthfpj_6ftPQGvjL3qO3x4qhz6TukuI0EvIvq9SNBLXXVQ3LNHCDtmOkO4MWc-k0hHLOxmrAwyLl47NvSRgXgS3N6xvcNM9NFQ9REz0UXNQZXpji-pFJfWaZJ_Y7BXdpCujdezFfrscPU2LdIuWhg6Ok10DsT4F2YmsHOnnSob1peOxBHOgLULlbUmTLwmzM5iBsODo4JEw4GFTNpE5WGQsZbV7zmStw2FXvf24rwrd2l9vQp3MMJAnoV0O3-7Nz4Wt1CdqMPjPDetl7R7KQObK3jC_p_LeeisdyfSaHso7bMnerZELy4tHoI_GFJoZeZ7DREwaITg1EQyBIdWMGE2HDYZAE45-8YqsoNn84kuhGLHQ0taDqjEQvqwVLRBVAbNk3O9uO6kF9nz-P_P0Dd9iHKxtkEXl7P0-TWYrJ1vhoWw3vdOt7GcXm9Z-4wqwAGcQEnjhrQ5pEUaVjoytWk-taHzsKkQsS6fXo8xkddAJsi31Qek-8Y047Y5YL7eT66sTWAhcwAtmy2XsrbxbUNnZp0klqYn5ohYdY2C1pwQPbd1eWGOUmIEATlioZNRQoORnpBREBxgj5CIN6yxznDgeWOsB0G0pcUam1NE9Lq_2UhqKwhVkHLvaGM9PC1ACB6sB-tGLW5t6zG2j8WkpFXkOSP9_I4RnV4y6nSU5PdpntlaLY10z7R0fW7onpFM2oWlOfHjfHolSjm_3WjBIXMfriVnm9we1BD9kiJRso2TzeLHH39Z-sC7kRx1XW0X6bVxn4pboV07x-vJQ5Hr1_fp8CY43suGhI1fv-5WV9stYjf8jlp47OGwlsdkybVrbEHRlzsx37WDamOtqCxvrvR1uFbG6Ypoj4rS3oabB3Ez3Yyv0X78a2KXOwMbm6FeQSp32e2P8lgeFvebqXJdacyLLR-WGfNA3kl_38y3lX1z5WrJTTXGboCOqXx1yN3HFjv8b0m6JeY6B-0qxN6tRLqSY4wH03j7a1xV03V4X7p3SivNZfVeEU52Y1yjxLWd0IsD6_pQ_xjF9-4PfMDmYpxp6jDSRg_ZeGtTezkaHIPJ2KPrwnUr1L8QNil-eTn8XzW7Y3c=");
		executionContext.put("body", "{\"server\": {\"name\": \"server-test-X\",\"imageRef\": \"56ff0279-f1fb-46e5-93dc-fe7093af0b1a\",\"flavorRef\": \"2\",\"max_count\": 1,\"min_count\": 1,\"security_groups\": [{\"name\": \"default\"}]}}");
		executionContext.put("contentType", "application/json");


		//sendEmail
		executionContext.put("receiver", "receiverTest");
		executionContext.put("title", "titleTest");

	}

	private static OpenstackApplications loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/openstackApplicationContext.xml");
		OpenstackApplications app = context.getBean(OpenstackApplications.class);
		app.context  = context;
		return app;
	}

	private void closeContext() {
		((ConfigurableApplicationContext) context).close();
	}

	private void registerEventListeners() {
		//register listener for action runtime events
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
		registerInfoEventListener(handlerTypes);

		//register listener for action exception events
		registerExceptionEventListener();

		// for closing the Application Context when score finishes execution
		registerScoreEventListener();
	}

	private void registerExceptionEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerInfoEventListener(Set<String> handlerTypes) {
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add("FINISHED");
		handlerTypes.add("ERROR");
		handlerTypes.add("CANCELLED");
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
				closeContext();
			}
		}, handlerTypes);
	}

	private void logListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}
}
