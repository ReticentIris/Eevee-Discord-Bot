# Eevee Discord Bot

[![Build Status](https://travis-ci.org/ReticentIris/Eevee-Discord-Bot.svg?branch=master)](https://travis-ci.org/ReticentIris/Eevee-Discord-Bot)

A Discord bot consisting of whatever I find useful.

## Command Architecture

Commands are declaratively defined using a system similar to [Dratini](https://github.com/ReticentIris/Dratini).

## Runtime Environment Requirements
- JRE 8
- MongoDB (running on localhost using the default port `27017`)
- Google Cloud Platform Credential File @ `conf/Eevee.Google.json`

## Automated Deployment Pipeline
1. Git Commit Pushed
2. Travis Triggered
3. Project Compiled
4. Docker Image Built
5. Docker Image Pushed
6. Docker Image Update Detected
7. Docker Image Pulled
8. Existing Docker Container Swapped