var socket = null;
var connected = false;
      var socket;

      $( document ).ready(function() {
          connect();
          $("#send").click(sendMessage);

          $("#name").keypress(function(event){
              if(event.keyCode == 13 || event.which == 13) {
                  connect();
              }
          });

          $("#msg").keypress(function(event) {
              if(event.keyCode == 13 || event.which == 13) {
                  sendMessage();
              }
          });

        $("#chat").change(function() {
            scrollToBottom();
          });

          $("#name").focus();
      });

      var connect = function() {
          if (! connected) {
              socket = new WebSocket("ws://" + location.host + "/api/chat");
              socket.onopen = function() {
                  connected = true;
                  console.log("Connected to the web socket");
                  $("#send").attr("disabled", false);
                  $("#connect").attr("disabled", true);
                  $("#name").attr("disabled", true);
                  $("#msg").focus();
              };
              socket.onmessage =function(m) {
                  console.log("Got message: " + m.data);
                  $("#chat").append(m.data + "\n");
                  scrollToBottom();
              };
          }
      };

      var sendMessage = function() {
          if (connected) {
              var value = $("#msg").val();
              console.log("Sending " + value);
              socket.send(value);
              $("#msg").val("");
          }
      };

      var scrollToBottom = function () {
        $('#chat').scrollTop($('#chat')[0].scrollHeight);
      };

document.addEventListener("DOMContentLoaded", function() {
    fetchData();           // Fetch dashboard data
});



function fetchData() {
    fetch('/api/dashboard', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => renderData(data))
    .catch(error => console.error('Error fetching data:', error));
}

function renderData(data) {
    const analysisContainer = document.getElementById('analysis-container');
    const feedbacksContainer = document.getElementById('feedbacks-container');

    data.analysis.forEach(item => {
        const analysisDiv = document.createElement('div');
        analysisDiv.className = 'analysis';
        analysisDiv.innerHTML = `<strong>${item.name}</strong>: ` + JSON.stringify(item.values);
        analysisContainer.appendChild(analysisDiv);
    });

    data.feedbacks.forEach(feedback => {
        const feedbackDiv = document.createElement('div');
        feedbackDiv.className = 'feedback';
        feedbackDiv.innerHTML = `<strong>${feedback.feedbackType}</strong>: ${feedback.feedback} <br>
                                 <strong>Impact</strong>: ${feedback.impact} <br>
                                 <strong>Severity</strong>: ${feedback.severity} <br>
                                 <strong>Urgency</strong>: ${feedback.urgency} <br>
                                 <strong>Categories</strong>: ${feedback.category}`;
        feedbacksContainer.appendChild(feedbackDiv);
    });
}