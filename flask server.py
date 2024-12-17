from flask import Flask, request, jsonify
import openai
import google.generativeai as genai
import re
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # CORS 설정
app.config['MAX_CONTENT_LENGTH'] = 32 * 1024 * 1024  # 요청 크기를 32MB로 설정

# ChatGPT API 키 설정
openai.api_key = ""

# Gemini 설정
genai.configure(api_key="")

def remove_markdown_formatting(text):
    """** 표시 및 기타 Markdown 포맷 제거"""
    if not text:
        return text
    text = re.sub(r"\*\*", "", text)
    text = re.sub(r"\*", "", text)
    return text

@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    history = data.get("history", [])

    if not history:
        return jsonify({"error": "History is empty"}), 400

    # ChatGPT 응답 생성
    try:
        chatgpt_response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=history,
            max_tokens=150,
            temperature=0.7
        )['choices'][0]['message']['content']
        chatgpt_response = remove_markdown_formatting(chatgpt_response)
    except Exception as e:
        chatgpt_response = f"ChatGPT Error: {str(e)}"

    # Gemini 응답 생성
    try:
        gemini_model = genai.GenerativeModel("gemini-pro")
        gemini_prompt = "\n".join([f"{msg['role']}: {msg['content']}" for msg in history])
        gemini_response = gemini_model.generate_content(gemini_prompt).text.strip()
        gemini_response = remove_markdown_formatting(gemini_response)
    except Exception as e:
        gemini_response = f"Gemini Error: {str(e)}"

    return jsonify({
        "chatgpt_response": chatgpt_response,
        "gemini_response": gemini_response
    })

def truncate_text(text, max_length=50):
    """응답을 최대 max_length 길이로 자르기"""
    return text[:max_length] + ("..." if len(text) > max_length else "")

# Debate 엔드포인트

def remove_markdown_formatting(text):
    """Markdown 포맷 제거"""
    if not text:
        return text
    text = re.sub(r"\*\*", "", text)
    text = re.sub(r"\*", "", text)
    return text

@app.route('/debate', methods=['POST'])
def debate():
    data = request.json
    topic = data.get("topic")
    history = data.get("history", [])

    if not topic:
        return jsonify({"error": "Topic is empty"}), 400

    # 현재 진행된 라운드 수 계산
    round_count = len(history) // 2

    if round_count >= 5:  # 각 봇이 5번씩 총 10번의 발언을 마치면 종료
        return jsonify({
            "gemini_response": "토론이 종료되었습니다.",
            "chatgpt_response": "토론이 종료되었습니다.",
            "history": history
        })

    # 현재 차례 결정 (ChatGPT 또는 Gemini)
    current_speaker = "ChatGPT" if len(history) % 2 == 0 else "Gemini"

    gemini_response = ""
    chatgpt_response = ""

    # ChatGPT 차례
    if current_speaker == "ChatGPT":
        try:
            chatgpt_prompt = [
                {"role": "system", "content": f"You are participating in a debate on the topic '{topic}' and should respond concisely."}
            ]
            for msg in history:
                role = "assistant" if msg['role'] == "ChatGPT" else "user"
                chatgpt_prompt.append({"role": role, "content": msg['content']})

            response = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=chatgpt_prompt,
                max_tokens=200,
                temperature=0.7
            )
            chatgpt_response = response['choices'][0]['message']['content'].strip()
            chatgpt_response = remove_markdown_formatting(chatgpt_response)
            history.append({"role": "ChatGPT", "content": chatgpt_response})

        except Exception as e:
            chatgpt_response = f"ChatGPT Error: {str(e)}"
            history.append({"role": "ChatGPT", "content": chatgpt_response})

    # Gemini 차례
    else:
        try:
            history_str = "\n".join([f"{msg['role']}: {msg['content']}" for msg in history])
            gemini_prompt = f"{history_str}\nGemini: {topic}에 대해 이전 논점을 반박하거나 새로운 관점으로 의견을 제시해 주세요."
            gemini_model = genai.GenerativeModel("gemini-pro")
            gemini_response = gemini_model.generate_content(gemini_prompt).text.strip()
            gemini_response = remove_markdown_formatting(gemini_response)
            history.append({"role": "Gemini", "content": gemini_response})

        except Exception as e:
            gemini_response = f"Gemini Error: {str(e)}"
            history.append({"role": "Gemini", "content": gemini_response})

    return jsonify({
        "gemini_response": gemini_response,
        "chatgpt_response": chatgpt_response,
        "history": history
    })

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)