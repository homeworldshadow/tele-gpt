# Tele-gpt

## Overview

The [Telegram](https://telegram.org/) chat-bot to interact with [OpenAI platform](https://platform.openai.com/overview).

## Requirements

To use this chat-bot it is required to have
- Telegram chat-bot [authentication token](https://core.telegram.org/bots/api#authorizing-your-bot)
- OpenAI [authentication API key](https://platform.openai.com/docs/api-reference/authentication)

## Features

- send text messages and get text responses
- send voice messages and get voice or text responses
- send request to generate an image

## Build

Use gradle
- ./gradlew clean build docker

## Run

Use docker compose
- ./docker-compose up -d

## Configs

Configs could be defined with following options:
- using __application.properties__ file
- using __system environment__ properties. Properties in system environment overrides application.properties.

Properties
- __server.port__ - server port (integer)
- __tg.api_key__ -  Telegram authentication token (string)
- __tg.retry.max__ - Max retries to process received message (integer)
- __tg.retry.timeout__ - Timeout between retries (duration string)
- __gpt.api_key__ - OpenAI authentication API key (string)
- __gpt.image.size__ - image size to generate (string, see [spec](https://platform.openai.com/docs/api-reference/images/create#images/create-size))
- __gpt.model__ - GPT chat model (string, see [spec](https://platform.openai.com/docs/api-reference/chat/create#chat/create-model))
- __gpt.client.read-timeout__ - GPT client timeout (duration string)
- __opentts.url__ - OpenTTS URL (URL string)
- __opentts.timeout__ - OpenTTS response timeout (duration string)
- __opentts.quality__ - Voice quality (string: low, medium, high). Default: medium
- __opentts.tts-voice.XX__ - OpenTTS voice coder, where XX is ISO2 lang. Example:

        opentts.tts-voice.en=larynx:blizzard_fls-glow_tts
        opentts.tts-voice.ru=larynx:hajdurova-glow_tts
  

## Team

- Eugene Bayura ([homeworldshadow@gmail.com](mailto:homeworldshadow@gmail.com))

## License

GNU General Public License v3

    https://www.gnu.org/licenses/gpl-3.0.en.html

