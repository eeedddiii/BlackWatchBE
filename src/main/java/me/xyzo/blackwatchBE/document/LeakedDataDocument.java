package me.xyzo.blackwatchBE.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "leaked_data")
public class LeakedDataDocument {

    @Id
    private String id;

    @Field("clientId")
    private String clientId;

    private String host;
    private String path;
    private String title;
    private String author;

    @Field("uploadDate")
    private LocalDateTime uploadDate;

    @Field("leakType")
    private String leakType;

    @Field("recordsCount")
    private Integer recordsCount;

    private String iocs;
    private String price;
    private String article;
    private List<String> ref;

    @Field("leakedEmail")
    private List<String> leakedEmail;

    @Field("leakedName")
    private List<String> leakedName;

    @Field("createdAt")
    private LocalDateTime createdAt;

    // constructors
    public LeakedDataDocument() {}

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getLeakType() { return leakType; }
    public void setLeakType(String leakType) { this.leakType = leakType; }

    public Integer getRecordsCount() { return recordsCount; }
    public void setRecordsCount(Integer recordsCount) { this.recordsCount = recordsCount; }

    public String getIocs() { return iocs; }
    public void setIocs(String iocs) { this.iocs = iocs; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public List<String> getRef() { return ref; }
    public void setRef(List<String> ref) { this.ref = ref; }

    public List<String> getLeakedEmail() { return leakedEmail; }
    public void setLeakedEmail(List<String> leakedEmail) { this.leakedEmail = leakedEmail; }

    public List<String> getLeakedName() { return leakedName; }
    public void setLeakedName(List<String> leakedName) { this.leakedName = leakedName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
