package edu.pjatk.app.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`file`")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String file_name;
    private Long file_size;
    private String file_url;


    public File(String file_name, Long file_size, String file_url){
        this.file_name = file_name;
        this.file_size = file_size;
        this.file_url = file_url;
    }
}
