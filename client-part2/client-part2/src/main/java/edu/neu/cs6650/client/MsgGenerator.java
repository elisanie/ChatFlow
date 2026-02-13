package edu.neu.cs6650.client;

import edu.neu.cs6650.client.model.ChatMessage;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class MsgGenerator implements Runnable {
    // Single dedicated thread generates all messages
    // Generate 500,000 chat messages total with random data
    // message: random from a pool of 50 pre-defined messages
    private static final int TOTAL_MSGS = 500000;
    private static final String[] MSG_POOL = {
            "The legend of Hyrule never gets old",
            "Time to explore Hyrule again",
            "The Master Sword feels powerful",
            "Heading toward Hyrule Castle",
            "The Triforce is calling",
            "Just solved another shrine puzzle",
            "Guardians are everywhere",
            "Breath of the Wild is amazing",
            "Cooking meals before the journey",
            "Lost in the vast open world",

            "Zelda is waiting somewhere",
            "Found a hidden Korok seed",
            "The Sheikah Slate is useful",
            "Climbing mountains takes stamina",
            "Weather changes everything",
            "Another Blood Moon is rising",
            "Discovered a secret passage",
            "The music feels nostalgic",
            "Preparing for the final battle",
            "The world feels alive",

            "Found a new shrine challenge",
            "Horses make traveling faster",
            "Ancient technology is fascinating",
            "The map keeps expanding",
            "Every village has a story",
            "Rupees are hard to save",
            "Weapons break too quickly",
            "Enemy camps are dangerous",
            "The scenery is breathtaking",
            "Adventure awaits everywhere",

            "Solving puzzles is satisfying",
            "Exploring ruins takes patience",
            "This quest is challenging",
            "Learning enemy patterns",
            "The story unfolds slowly",
            "Found a rare weapon",
            "The journey feels endless",
            "Courage is tested often",
            "Wisdom guides the way",
            "Power must be controlled",

            "The legend continues",
            "Destiny feels unavoidable",
            "Another side quest completed",
            "Nighttime feels more dangerous",
            "Traveling light is risky",
            "The world rewards curiosity",
            "Every path tells a story",
            "Preparing for what lies ahead",
            "Hyrule is full of secrets",
            "The adventure never truly ends"
    };

    //    Places messages in a thread-safe queue/buffer for producer & consumer
    //    roomId: random between 1-20
    private final LinkedBlockingQueue<ChatMessage> queue;
    private final Random random = new Random();

    public MsgGenerator(LinkedBlockingQueue<ChatMessage> queue) {
        this.queue = queue;
    }


    @Override
    public void run() {
        try {
            for (int i = 0; i < TOTAL_MSGS; i++) {
                // username: generate from userId (e.g., "user12345")
                int userId = random.nextInt(100000) + 1;
                String username = "user" + userId;
                String msg = MSG_POOL[random.nextInt(MSG_POOL.length)];
                //  roomId: random between 1-20
                String roomId = String.valueOf(random.nextInt(20) + 1);
                String msgType = randomMsgType();
                // timestamp: current time
                String timestamp = Instant.now().toString();

                ChatMessage chat = new ChatMessage(userId + "", username, msg,
                        timestamp, msgType, roomId);
                queue.put(chat);
            }
            System.out.println("Message Generator: all " + TOTAL_MSGS + " messages generated.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Message Generator interrupted.");
        }
    }

    // messageType: 90% TEXT, 5% JOIN, 5% LEAVE
    private String randomMsgType() {
        int roll = random.nextInt(100);
        if (roll < 90) return "TEXT";
        if (roll < 95) return "JOIN";
        return "LEAVE";
    }



}
