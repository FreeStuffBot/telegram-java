/**
 * A system for processing events, built for Telegram updates.
 * <p>
 * It allows bots to be more modular and object-oriented, by defining a shared interface for updates handlers.
 * <p>
 * The classes defined here can be used with threading, except the {@code register} and {@code unregister} methods
 * which can result in an undefined behaviour for them, and for concurrent {@code process} calls.
 */
package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;